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
    private val legacyFamilyRoot = ".drivebox_family"

    override fun serve(session: IHTTPSession): Response = try {
        when {
            session.uri == "/" || session.uri == "/index.html" -> asset("drivebox/index.html", "text/html; charset=utf-8")
            session.uri == "/api/family/profiles" && session.method == Method.GET -> profiles()
            session.uri == "/api/family/create" && session.method == Method.POST -> createMember(session)
            session.uri == "/api/family/setup" && session.method == Method.POST -> setupMember(session)
            session.uri == "/api/login" && session.method == Method.POST -> login(session)
            session.uri.startsWith("/api/") -> {
                val token = bearer(session) ?: session.parameters["token"]?.firstOrNull()
                val principal = auth.principal(token)
                if (principal == null) {
                    json(Response.Status.UNAUTHORIZED, JSONObject().put("error", "Authentification requise"))
                } else {
                    api(session, token ?: "", principal)
                }
            }
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }
    } catch (e: Exception) {
        json(Response.Status.BAD_REQUEST, JSONObject().put("error", e.message ?: "Erreur"))
    }

    private fun profiles(): Response {
        val array = JSONArray()
        users.profiles().forEach { array.put(users.publicJson(it)) }
        return json(
            Response.Status.OK,
            JSONObject()
                .put("version", "1.3.2")
                .put("profiles", array)
                .put("storageReady", storage.hasRoot())
        )
    }

    private fun createMember(session: IHTTPSession): Response {
        val body = body(session)
        val password = body.optString("password")
        require(password == body.optString("confirm")) { "Les mots de passe ne correspondent pas" }
        val member = users.create(body.optString("displayName"), password)
        return openSession(member)
    }

    private fun setupMember(session: IHTTPSession): Response {
        val body = body(session)
        val password = body.optString("password")
        require(password == body.optString("confirm")) { "Les mots de passe ne correspondent pas" }
        val member = users.setupPassword(body.optString("memberId"), password)
        return openSession(member)
    }

    private fun openSession(member: UserManager.Member): Response {
        val token = auth.startMemberSession(member)
        val principal = auth.principal(token) ?: throw IllegalStateException("Session impossible")
        val storageWarning = safeEnsureMemberRoot(principal)
        return json(
            Response.Status.OK,
            JSONObject()
                .put("token", token)
                .put("user", userJson(principal))
                .put("member", users.publicJson(member))
                .put("storageReady", storageWarning == null && storage.hasRoot())
                .put("storageWarning", storageWarning ?: JSONObject.NULL)
        )
    }

    private fun login(session: IHTTPSession): Response {
        val body = body(session)
        val memberId = body.optString("memberId")
        val password = body.optString("password")
        val existing = users.findById(memberId)
            ?: return json(Response.Status.UNAUTHORIZED, JSONObject().put("error", "Profil introuvable"))

        val member = if (existing.hash.isBlank() || existing.salt.isBlank()) {
            try {
                users.setupPassword(memberId, password)
            } catch (e: Exception) {
                return json(Response.Status.BAD_REQUEST, JSONObject().put("error", e.message ?: "Impossible d'enregistrer le mot de passe"))
            }
        } else {
            users.verifyById(memberId, password)
                ?: return json(Response.Status.UNAUTHORIZED, JSONObject().put("error", "Mot de passe incorrect"))
        }

        return openSession(member)
    }

    private fun api(session: IHTTPSession, token: String, principal: AuthPrincipal): Response = when (session.uri) {
        "/api/status" -> {
            val warning = safeEnsureMemberRoot(principal)
            json(
                Response.Status.OK,
                JSONObject()
                    .put("ok", true)
                    .put("name", "DriveBox Family")
                    .put("version", "1.3.2")
                    .put("ip", NetworkUtils.localIp())
                    .put("port", AppConfig.PORT)
                    .put("storageReady", warning == null && storage.hasRoot())
                    .put("storageWarning", warning ?: JSONObject.NULL)
                    .put("user", userJson(principal))
            )
        }
        "/api/list" -> json(Response.Status.OK, listFor(principal, param(session, "path")))
        "/api/mkdir" -> {
            method(session, Method.POST)
            val b = body(session)
            storage.createFolder(scope(principal, b.optString("path", "")), b.getString("name"))
            ok()
        }
        "/api/rename" -> {
            method(session, Method.POST)
            val b = body(session)
            storage.rename(scope(principal, b.getString("path")), b.getString("name"))
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
        "/api/logout" -> {
            method(session, Method.POST)
            auth.logout(token)
            ok()
        }
        else -> json(Response.Status.NOT_FOUND, JSONObject().put("error", "API inconnue"))
    }

    private fun changePassword(session: IHTTPSession, token: String, principal: AuthPrincipal): Response {
        method(session, Method.POST)
        val b = body(session)
        val newPassword = b.optString("newPassword")
        require(newPassword == b.optString("confirm")) { "Les nouveaux mots de passe ne correspondent pas" }
        users.changePassword(principal.memberId, b.optString("currentPassword"), newPassword)
        auth.logout(token)
        return json(Response.Status.OK, JSONObject().put("ok", true).put("reauthenticate", true))
    }

    private fun safeEnsureMemberRoot(principal: AuthPrincipal): String? = try {
        ensureMemberRoot(principal)
        null
    } catch (e: Exception) {
        "Compte connecté, mais stockage indisponible : ${e.message ?: "écriture impossible"}"
    }

    private fun ensureMemberRoot(principal: AuthPrincipal) {
        if (!storage.hasRoot()) return

        val legacyBase = "$legacyFamilyRoot/${principal.storageKey}"
        if (storage.resolve(legacyBase) != null) return

        if (storage.resolve(familyRoot) == null) storage.createFolder("", familyRoot)
        val base = "$familyRoot/${principal.storageKey}"
        if (storage.resolve(base) == null) storage.createFolder(familyRoot, principal.storageKey)
        listOf("Photos", "Vidéos", "Documents", "Musique").forEach { folder ->
            if (storage.resolve("$base/$folder") == null) {
                try { storage.createFolder(base, folder) } catch (_: Exception) { }
            }
        }
    }

    private fun basePath(principal: AuthPrincipal): String {
        val legacyBase = "$legacyFamilyRoot/${principal.storageKey}"
        return if (storage.resolve(legacyBase) != null) legacyBase else "$familyRoot/${principal.storageKey}"
    }

    private fun scope(principal: AuthPrincipal, path: String): String {
        val visible = normalizeVisible(path)
        require(visible.split('/').none { it == "." || it == ".." }) { "Chemin interdit" }
        ensureMemberRoot(principal)
        val prefix = basePath(principal)
        return if (visible.isBlank()) prefix else "$prefix/$visible"
    }

    private fun normalizeVisible(path: String): String = path.trim().trim('/').replace(Regex("/+"), "/")

    private fun listFor(principal: AuthPrincipal, path: String): JSONObject {
        val visiblePath = normalizeVisible(path)
        val scoped = scope(principal, visiblePath)
        val data = storage.list(scoped)
        val source = data.getJSONArray("items")
        val result = JSONArray()
        val prefix = basePath(principal) + "/"
        for (i in 0 until source.length()) {
            val copy = JSONObject(source.getJSONObject(i).toString())
            val storedPath = copy.optString("path")
            copy.put("path", if (storedPath.startsWith(prefix)) storedPath.removePrefix(prefix) else "")
            result.put(copy)
        }
        return JSONObject().put("path", visiblePath).put("items", result)
    }

    private fun upload(session: IHTTPSession, principal: AuthPrincipal): Response {
        method(session, Method.POST)
        val files = HashMap<String, String>()
        session.parseBody(files)
        val tempPath = files["file"] ?: throw IllegalArgumentException("Fichier manquant")
        val originalName = session.parameters["file"]?.firstOrNull()
            ?: session.headers["x-file-name"]
            ?: throw IllegalArgumentException("Nom du fichier manquant")
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
        val response = newChunkedResponse(Response.Status.OK, doc.type ?: "application/octet-stream", input)
        val safeName = (doc.name ?: "download").replace("\"", "")
        val disposition = if (session.parameters["download"]?.firstOrNull() == "1") "attachment" else "inline"
        response.addHeader("Content-Disposition", "$disposition; filename=\"$safeName\"")
        response.addHeader("Cache-Control", "private, max-age=60")
        return response
    }

    private fun userJson(principal: AuthPrincipal): JSONObject = JSONObject()
        .put("id", principal.memberId)
        .put("username", principal.username)
        .put("displayName", principal.displayName)

    private fun bearer(session: IHTTPSession): String? = session.headers["authorization"]
        ?.takeIf { it.startsWith("Bearer ", true) }
        ?.substring(7)
        ?.trim()

    private fun param(session: IHTTPSession, key: String): String = session.parameters[key]?.firstOrNull() ?: ""

    private fun body(session: IHTTPSession): JSONObject {
        val files = HashMap<String, String>()
        session.parseBody(files)
        return JSONObject(files["postData"] ?: "{}")
    }

    private fun method(session: IHTTPSession, method: Method) {
        if (session.method != method) throw IllegalArgumentException("Méthode HTTP invalide")
    }

    private fun ok(): Response = json(Response.Status.OK, JSONObject().put("ok", true))

    private fun json(status: Response.Status, body: JSONObject): Response =
        newFixedLengthResponse(status, "application/json; charset=utf-8", body.toString()).apply {
            addHeader("Cache-Control", "no-store")
            addHeader("X-Content-Type-Options", "nosniff")
            addHeader("X-Frame-Options", "SAMEORIGIN")
        }

    private fun asset(path: String, mime: String): Response =
        newFixedLengthResponse(Response.Status.OK, mime, context.assets.open(path).bufferedReader().use { it.readText() }).apply {
            addHeader("Cache-Control", "no-store")
            addHeader("X-Content-Type-Options", "nosniff")
        }
}
