package lu.guerrida.drivebox.server

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Method
import fi.iki.elonen.NanoHTTPD.Response
import lu.guerrida.drivebox.AppConfig
import lu.guerrida.drivebox.NetworkUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

class DriveBoxHttpServer(private val context: Context, port: Int = AppConfig.PORT) : NanoHTTPD(port) {
    private val users = UserManager(context)
    private val auth = AuthManager(users)
    private val storage = StorageRepository(context)
    private val familyRoot = "DriveBoxFamily"

    override fun serve(session: IHTTPSession): Response = try {
        when {
            session.uri == "/" || session.uri == "/index.html" -> asset("drivebox/index.html", "text/html; charset=utf-8")
            session.uri == "/api/family/profiles" && session.method == Method.GET -> profiles()
            session.uri == "/api/family/create" && session.method == Method.POST -> createMember(session)
            session.uri == "/api/login" && session.method == Method.POST -> login(session)
            session.uri.startsWith("/api/") -> {
                val token = bearer(session) ?: session.parameters["token"]?.firstOrNull()
                val principal = auth.principal(token)
                if (principal == null) json(Response.Status.UNAUTHORIZED, JSONObject().put("error", "Authentification requise"))
                else api(session, token ?: "", principal)
            }
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }
    } catch (e: Exception) {
        json(Response.Status.BAD_REQUEST, JSONObject().put("error", e.message ?: e.javaClass.simpleName))
    }

    private fun profiles(): Response {
        val array = JSONArray()
        users.profiles().forEach { array.put(users.publicJson(it)) }
        return json(Response.Status.OK, JSONObject().put("version", "2.0.0-preview").put("profiles", array).put("storage", storage.status()))
    }

    private fun createMember(session: IHTTPSession): Response {
        val b = body(session)
        val password = b.optString("password")
        require(password == b.optString("confirm")) { "Les mots de passe ne correspondent pas" }
        return openSession(users.create(b.optString("displayName"), password))
    }

    private fun login(session: IHTTPSession): Response {
        val b = body(session)
        val id = b.optString("memberId")
        val password = b.optString("password")
        val existing = users.findById(id)
            ?: return json(Response.Status.UNAUTHORIZED, JSONObject().put("error", "Profil introuvable"))
        val member = if (existing.hash.isBlank() || existing.salt.isBlank()) {
            try { users.setupPassword(id, password) }
            catch (e: Exception) { return json(Response.Status.BAD_REQUEST, JSONObject().put("error", e.message ?: "Mot de passe invalide")) }
        } else {
            users.verifyById(id, password)
                ?: return json(Response.Status.UNAUTHORIZED, JSONObject().put("error", "Mot de passe incorrect"))
        }
        return openSession(member)
    }

    private fun openSession(member: UserManager.Member): Response {
        val token = auth.startMemberSession(member)
        val principal = auth.principal(token) ?: throw IllegalStateException("Session impossible")
        val warning = ensureMemberRootSafe(principal)
        return json(Response.Status.OK, JSONObject().put("token", token).put("user", userJson(principal))
            .put("storage", storage.status()).put("storageWarning", warning ?: JSONObject.NULL))
    }

    private fun api(session: IHTTPSession, token: String, principal: AuthPrincipal): Response = when (session.uri) {
        "/api/status" -> {
            val warning = ensureMemberRootSafe(principal)
            json(Response.Status.OK, JSONObject().put("ok", true).put("version", "2.0.0-preview")
                .put("ip", NetworkUtils.localIp()).put("port", AppConfig.PORT).put("user", userJson(principal))
                .put("storage", storage.status()).put("storageWarning", warning ?: JSONObject.NULL))
        }
        "/api/storage/status" -> json(Response.Status.OK, storage.status())
        "/api/insights" -> json(Response.Status.OK, insights(principal))
        "/api/smart-search" -> json(Response.Status.OK, smartSearch(principal, param(session, "q"), param(session, "kind")))
        "/api/ai/query" -> { method(session, Method.POST); json(Response.Status.OK, aiQuery(principal, body(session).optString("query"))) }
        "/api/list" -> json(Response.Status.OK, listFor(principal, param(session, "path")))
        "/api/mkdir" -> {
            method(session, Method.POST)
            val b = body(session)
            storage.createFolder(scope(principal, b.optString("path")), b.getString("name").trim())
            ok()
        }
        "/api/rename" -> {
            method(session, Method.POST)
            val b = body(session)
            storage.rename(scope(principal, b.getString("path")), b.getString("name").trim())
            ok()
        }
        "/api/delete" -> {
            method(session, Method.POST)
            val b = body(session)
            storage.delete(scope(principal, b.getString("path")))
            ok()
        }
        "/api/upload" -> upload(session, principal)
        "/api/download" -> download(session, principal)
        "/api/profile/password" -> changePassword(session, token, principal)
        "/api/logout" -> { method(session, Method.POST); auth.logout(token); ok() }
        else -> json(Response.Status.NOT_FOUND, JSONObject().put("error", "API inconnue"))
    }

    private data class ScanItem(val name: String, val path: String, val size: Long, val mime: String, val modified: Long)
    private data class ScanResult(val files: MutableList<ScanItem> = mutableListOf(), var folders: Int = 0, var limited: Boolean = false)

    private fun scan(principal: AuthPrincipal, maxFiles: Int = 5000): ScanResult {
        val result = ScanResult()
        val root = storage.resolve(scope(principal, "")) ?: return result
        fun walk(dir: DocumentFile, rel: String, depth: Int) {
            if (depth > 16 || result.files.size >= maxFiles) { result.limited = true; return }
            dir.listFiles().forEach { child ->
                if (result.files.size >= maxFiles) { result.limited = true; return@forEach }
                val name = child.name ?: return@forEach
                val p = if (rel.isBlank()) name else "$rel/$name"
                if (child.isDirectory) {
                    result.folders++
                    walk(child, p, depth + 1)
                } else if (child.isFile) {
                    result.files.add(ScanItem(name, p, child.length(), child.type ?: guessMime(name), child.lastModified()))
                }
            }
        }
        walk(root, "", 0)
        return result
    }

    private fun insights(principal: AuthPrincipal): JSONObject {
        val s = scan(principal)
        val total = s.files.sumOf { it.size }
        val cats = linkedMapOf("images" to 0, "videos" to 0, "audio" to 0, "documents" to 0, "archives" to 0, "other" to 0)
        s.files.forEach { cats[category(it)] = (cats[category(it)] ?: 0) + 1 }
        val largest = s.files.sortedByDescending { it.size }.take(8)
        val recent = s.files.sortedByDescending { it.modified }.take(8)
        val dupGroups = s.files.filter { it.size > 0 }.groupBy { "${it.size}:${it.name.lowercase(Locale.ROOT)}" }.values.filter { it.size > 1 }
        val reclaimable = dupGroups.sumOf { group -> group.first().size * (group.size - 1) }
        val suggestions = JSONArray()
        if (dupGroups.isNotEmpty()) suggestions.put("${dupGroups.size} groupe(s) de doublons probables détecté(s)")
        val huge = s.files.count { it.size >= 500L * 1024 * 1024 }
        if (huge > 0) suggestions.put("$huge fichier(s) de plus de 500 Mo occupent beaucoup d’espace")
        if ((cats["archives"] ?: 0) > 10) suggestions.put("Beaucoup d’archives ZIP/RAR : un nettoyage peut libérer de l’espace")
        if (suggestions.length() == 0) suggestions.put("Ton espace est bien organisé. DriveBox AI continuera à surveiller les gros fichiers.")
        return JSONObject()
            .put("files", s.files.size).put("folders", s.folders).put("bytes", total).put("limited", s.limited)
            .put("duplicates", dupGroups.sumOf { it.size - 1 }).put("duplicateGroups", dupGroups.size).put("reclaimableBytes", reclaimable)
            .put("categories", JSONObject(cats as Map<*, *>)).put("largest", itemsJson(largest)).put("recent", itemsJson(recent))
            .put("suggestions", suggestions).put("storage", storage.status())
    }

    private fun smartSearch(principal: AuthPrincipal, query: String, kind: String): JSONObject {
        val q = query.trim().lowercase(Locale.ROOT)
        val k = kind.trim().lowercase(Locale.ROOT)
        val all = scan(principal).files
        val matched = all.filter { item ->
            val nameOk = q.isBlank() || item.name.lowercase(Locale.ROOT).contains(q) || item.path.lowercase(Locale.ROOT).contains(q)
            val kindOk = k.isBlank() || k == "all" || category(item) == k || (k == "pdf" && item.mime == "application/pdf")
            nameOk && kindOk
        }.sortedByDescending { it.modified }.take(120)
        return JSONObject().put("query", query).put("kind", kind).put("count", matched.size).put("items", itemsJson(matched))
    }

    private fun aiQuery(principal: AuthPrincipal, raw: String): JSONObject {
        val q = raw.trim().lowercase(Locale.ROOT)
        require(q.isNotBlank()) { "Écris une demande" }
        val s = scan(principal)
        val files = s.files
        var answer: String
        var selected: List<ScanItem> = emptyList()
        var action = "message"
        when {
            listOf("doublon", "double").any { q.contains(it) } -> {
                val groups = files.filter { it.size > 0 }.groupBy { "${it.size}:${it.name.lowercase(Locale.ROOT)}" }.values.filter { it.size > 1 }
                selected = groups.flatten().take(40); action = "duplicates"
                answer = if (groups.isEmpty()) "Je n’ai trouvé aucun doublon probable." else "J’ai trouvé ${groups.size} groupe(s) de doublons probables. Vérifie-les avant suppression."
            }
            listOf("gros", "lourd", "plus grand").any { q.contains(it) } -> {
                selected = files.sortedByDescending { it.size }.take(20); action = "largest"
                answer = "Voici les fichiers qui occupent le plus d’espace."
            }
            listOf("récent", "recent", "dernier").any { q.contains(it) } -> {
                selected = files.sortedByDescending { it.modified }.take(20); action = "recent"
                answer = "Voici tes fichiers les plus récents."
            }
            listOf("vidéo", "video", "film").any { q.contains(it) } -> {
                selected = files.filter { category(it) == "videos" }.sortedByDescending { it.modified }.take(40); action = "videos"
                answer = "J’ai trouvé ${selected.size} vidéo(s) dans les premiers résultats."
            }
            listOf("photo", "image").any { q.contains(it) } -> {
                selected = files.filter { category(it) == "images" }.sortedByDescending { it.modified }.take(40); action = "images"
                answer = "Voici tes images et photos."
            }
            listOf("musique", "audio", "mp3").any { q.contains(it) } -> {
                selected = files.filter { category(it) == "audio" }.sortedByDescending { it.modified }.take(40); action = "audio"
                answer = "Voici tes fichiers audio."
            }
            q.contains("pdf") -> {
                selected = files.filter { it.mime == "application/pdf" || it.name.endsWith(".pdf", true) }.sortedByDescending { it.modified }.take(40); action = "pdf"
                answer = "Voici les PDF que j’ai trouvés."
            }
            listOf("zip", "archive", "rar").any { q.contains(it) } -> {
                selected = files.filter { category(it) == "archives" }.sortedByDescending { it.modified }.take(40); action = "archives"
                answer = "Voici tes archives ZIP/RAR/7Z."
            }
            listOf("stockage", "espace", "capacité", "capacite").any { q.contains(it) } -> {
                val total = files.sumOf { it.size }
                answer = "Ton espace contient ${files.size} fichier(s), ${s.folders} dossier(s), pour ${formatBytes(total)} de données visibles par DriveBox."
            }
            else -> {
                val terms = q.split(Regex("\\s+")).filter { it.length >= 2 && it !in setOf("trouve","cherche","montre","moi","les","des","dans","mon","mes") }
                selected = files.filter { f -> terms.isNotEmpty() && terms.all { t -> f.name.lowercase(Locale.ROOT).contains(t) || f.path.lowercase(Locale.ROOT).contains(t) } }.take(40)
                action = "search"
                answer = if (selected.isEmpty()) "Je n’ai rien trouvé. Essaie par exemple : « mes vidéos », « gros fichiers », « PDF », « doublons » ou un nom de fichier." else "J’ai trouvé ${selected.size} résultat(s) correspondant à ta demande."
            }
        }
        return JSONObject().put("answer", answer).put("action", action).put("items", itemsJson(selected))
    }

    private fun category(i: ScanItem): String {
        val m = i.mime.lowercase(Locale.ROOT)
        val ext = i.name.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return when {
            m.startsWith("image/") -> "images"
            m.startsWith("video/") -> "videos"
            m.startsWith("audio/") -> "audio"
            ext in setOf("zip","rar","7z","tar","gz") -> "archives"
            m == "application/pdf" || m.startsWith("text/") || ext in setOf("doc","docx","xls","xlsx","ppt","pptx","odt","ods","txt","pdf") -> "documents"
            else -> "other"
        }
    }

    private fun itemsJson(items: List<ScanItem>): JSONArray = JSONArray().apply {
        items.forEach { i -> put(JSONObject().put("name", i.name).put("path", i.path).put("size", i.size).put("mime", i.mime).put("modified", i.modified).put("directory", false)) }
    }

    private fun formatBytes(v: Long): String = when {
        v >= 1024L * 1024 * 1024 -> String.format(Locale.US, "%.1f Go", v / (1024.0 * 1024 * 1024))
        v >= 1024L * 1024 -> String.format(Locale.US, "%.1f Mo", v / (1024.0 * 1024))
        v >= 1024 -> String.format(Locale.US, "%.1f Ko", v / 1024.0)
        else -> "$v octets"
    }

    private fun guessMime(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }

    private fun ensureMemberRootSafe(principal: AuthPrincipal): String? = try { ensureMemberRoot(principal); null } catch (e: Exception) { e.message ?: "Stockage indisponible" }

    private fun ensureMemberRoot(principal: AuthPrincipal) {
        if (storage.resolve(familyRoot) == null) storage.createFolder("", familyRoot)
        val base = "$familyRoot/${principal.storageKey}"
        if (storage.resolve(base) == null) storage.createFolder(familyRoot, principal.storageKey)
        listOf("Photos", "Vidéos", "Documents", "Musique").forEach { name ->
            if (storage.resolve("$base/$name") == null) storage.createFolder(base, name)
        }
    }

    private fun scope(principal: AuthPrincipal, path: String): String {
        val visible = normalizeVisible(path)
        require(visible.split('/').none { it == "." || it == ".." }) { "Chemin interdit" }
        ensureMemberRoot(principal)
        val base = "$familyRoot/${principal.storageKey}"
        return if (visible.isBlank()) base else "$base/$visible"
    }

    private fun listFor(principal: AuthPrincipal, path: String): JSONObject {
        val visible = normalizeVisible(path)
        val data = storage.list(scope(principal, visible))
        val source = data.getJSONArray("items")
        val out = JSONArray()
        val prefix = "$familyRoot/${principal.storageKey}/"
        for (i in 0 until source.length()) {
            val item = JSONObject(source.getJSONObject(i).toString())
            val stored = item.optString("path")
            item.put("path", if (stored.startsWith(prefix)) stored.removePrefix(prefix) else "")
            out.put(item)
        }
        return JSONObject().put("path", visible).put("items", out).put("storage", storage.status())
    }

    private fun upload(session: IHTTPSession, principal: AuthPrincipal): Response {
        method(session, Method.POST)
        val files = HashMap<String, String>()
        session.parseBody(files)
        val tempPath = files["file"] ?: throw IllegalArgumentException("Fichier manquant")
        val originalName = session.parameters["file"]?.firstOrNull()?.substringAfterLast('/')?.substringAfterLast('\\')?.takeIf { it.isNotBlank() }
            ?: session.headers["x-file-name"]?.takeIf { it.isNotBlank() } ?: "fichier-${System.currentTimeMillis()}"
        val targetPath = session.parameters["path"]?.firstOrNull() ?: ""
        storage.importTempFile(scope(principal, targetPath), originalName, File(tempPath), session.headers["x-file-type"])
        return json(Response.Status.OK, JSONObject().put("ok", true).put("name", originalName))
    }

    private fun download(session: IHTTPSession, principal: AuthPrincipal): Response {
        val doc = storage.resolve(scope(principal, param(session, "path")))
            ?: return json(Response.Status.NOT_FOUND, JSONObject().put("error", "Fichier introuvable"))
        if (!doc.isFile) return json(Response.Status.BAD_REQUEST, JSONObject().put("error", "Ce n'est pas un fichier"))
        val input = storage.openInput(doc) ?: return json(Response.Status.INTERNAL_ERROR, JSONObject().put("error", "Lecture impossible"))
        return newChunkedResponse(Response.Status.OK, doc.type ?: "application/octet-stream", input).apply {
            val safe = (doc.name ?: "download").replace("\"", "")
            addHeader("Content-Disposition", (if (session.parameters["download"]?.firstOrNull() == "1") "attachment" else "inline") + "; filename=\"$safe\"")
            addHeader("Cache-Control", "private, max-age=60")
        }
    }

    private fun changePassword(session: IHTTPSession, token: String, principal: AuthPrincipal): Response {
        method(session, Method.POST)
        val b = body(session)
        val next = b.optString("newPassword")
        require(next == b.optString("confirm")) { "Les nouveaux mots de passe ne correspondent pas" }
        users.changePassword(principal.memberId, b.optString("currentPassword"), next)
        auth.logout(token)
        return json(Response.Status.OK, JSONObject().put("ok", true).put("reauthenticate", true))
    }

    private fun userJson(p: AuthPrincipal) = JSONObject().put("id", p.memberId).put("username", p.username).put("displayName", p.displayName)
    private fun bearer(s: IHTTPSession): String? = s.headers["authorization"]?.takeIf { it.startsWith("Bearer ", true) }?.substring(7)?.trim()
    private fun param(s: IHTTPSession, key: String) = s.parameters[key]?.firstOrNull() ?: ""
    private fun normalizeVisible(path: String) = path.trim().trim('/').replace(Regex("/+"), "/")
    private fun body(s: IHTTPSession): JSONObject { val f = HashMap<String, String>(); s.parseBody(f); return JSONObject(f["postData"] ?: "{}") }
    private fun method(s: IHTTPSession, m: Method) { if (s.method != m) throw IllegalArgumentException("Méthode HTTP invalide") }
    private fun ok() = json(Response.Status.OK, JSONObject().put("ok", true))
    private fun json(status: Response.Status, body: JSONObject) = newFixedLengthResponse(status, "application/json; charset=utf-8", body.toString()).apply { addHeader("Cache-Control", "no-store"); addHeader("X-Content-Type-Options", "nosniff") }
    private fun asset(path: String, mime: String) = newFixedLengthResponse(Response.Status.OK, mime, context.assets.open(path).bufferedReader().use { it.readText() }).apply { addHeader("Cache-Control", "no-store") }
}
