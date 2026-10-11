package lu.guerrida.drivebox.server

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Method
import fi.iki.elonen.NanoHTTPD.Response
import lu.guerrida.drivebox.AppConfig
import lu.guerrida.drivebox.NetworkUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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
        return json(Response.Status.OK, JSONObject()
            .put("version", "1.4.0")
            .put("profiles", array)
            .put("storage", storage.status()))
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
        return json(Response.Status.OK, JSONObject()
            .put("token", token)
            .put("user", userJson(principal))
            .put("storage", storage.status())
            .put("storageWarning", warning ?: JSONObject.NULL))
    }

    private fun api(session: IHTTPSession, token: String, principal: AuthPrincipal): Response = when (session.uri) {
        "/api/status" -> {
            val warning = ensureMemberRootSafe(principal)
            json(Response.Status.OK, JSONObject()
                .put("ok", true).put("version", "1.4.0")
                .put("ip", NetworkUtils.localIp()).put("port", AppConfig.PORT)
                .put("user", userJson(principal)).put("storage", storage.status())
                .put("storageWarning", warning ?: JSONObject.NULL))
        }
        "/api/storage/status" -> json(Response.Status.OK, storage.status())
        "/api/list" -> json(Response.Status.OK, listFor(principal, param(session, "path")))
        "/api/mkdir" -> {
            method(session, Method.POST)
            val b = body(session)
            storage.createFolder(scope(principal, b.optString("path")), b.getString("name").trim())
            json(Response.Status.OK, JSONObject().put("ok", true).put("items", listFor(principal, b.optString("path")).getJSONArray("items")))
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

    private fun ensureMemberRootSafe(principal: AuthPrincipal): String? = try {
        ensureMemberRoot(principal); null
    } catch (e: Exception) { e.message ?: "Stockage indisponible" }

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
        val originalName = session.parameters["file"]?.firstOrNull()?.substringAfterLast('/')
            ?.substringAfterLast('\\')?.takeIf { it.isNotBlank() }
            ?: session.headers["x-file-name"]?.takeIf { it.isNotBlank() }
            ?: "fichier-${System.currentTimeMillis()}"
        val targetPath = session.parameters["path"]?.firstOrNull() ?: ""
        storage.importTempFile(scope(principal, targetPath), originalName, File(tempPath), session.headers["x-file-type"])
        return json(Response.Status.OK, JSONObject().put("ok", true).put("name", originalName))
    }

    private fun download(session: IHTTPSession, principal: AuthPrincipal): Response {
        val doc = storage.resolve(scope(principal, param(session, "path")))
            ?: return json(Response.Status.NOT_FOUND, JSONObject().put("error", "Fichier introuvable"))
        if (!doc.isFile) return json(Response.Status.BAD_REQUEST, JSONObject().put("error", "Ce n'est pas un fichier"))
        val input = storage.openInput(doc)
            ?: return json(Response.Status.INTERNAL_ERROR, JSONObject().put("error", "Lecture impossible"))
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
