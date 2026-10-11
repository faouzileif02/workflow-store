package lu.guerrida.drivebox.server

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Method
import fi.iki.elonen.NanoHTTPD.Response
import lu.guerrida.drivebox.AppConfig
import lu.guerrida.drivebox.NetworkUtils
import org.json.JSONObject
import java.io.File

class DriveBoxHttpServer(private val context: Context, port: Int = AppConfig.PORT) : NanoHTTPD(port) {
    private val auth = AuthManager(context)
    private val storage = StorageRepository(context)

    override fun serve(session: IHTTPSession): Response = try {
        when {
            session.uri == "/" || session.uri == "/index.html" -> asset("drivebox/index.html", "text/html; charset=utf-8")
            session.uri == "/api/login" && session.method == Method.POST -> login(session)
            session.uri.startsWith("/api/") -> {
                val token = bearer(session) ?: session.parameters["token"]?.firstOrNull()
                if (!auth.valid(token)) json(Response.Status.UNAUTHORIZED, JSONObject().put("error", "Authentification requise"))
                else routeApi(session, token!!)
            }
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }
    } catch (e: Exception) {
        json(Response.Status.BAD_REQUEST, JSONObject().put("error", e.message ?: "Erreur"))
    }

    private fun routeApi(session: IHTTPSession, token: String): Response = when (session.uri) {
        "/api/status" -> json(Response.Status.OK, JSONObject().apply {
            put("ok", true); put("name", "DriveBox"); put("version", "1.1.0")
            put("ip", NetworkUtils.localIp()); put("port", AppConfig.PORT); put("storageReady", storage.hasRoot())
        })
        "/api/list" -> json(Response.Status.OK, storage.list(param(session, "path")))
        "/api/mkdir" -> { requireMethod(session, Method.POST); val b = bodyJson(session); storage.createFolder(b.optString("path", ""), b.getString("name")); json(Response.Status.OK, JSONObject().put("ok", true)) }
        "/api/rename" -> { requireMethod(session, Method.POST); val b = bodyJson(session); storage.rename(b.getString("path"), b.getString("name")); json(Response.Status.OK, JSONObject().put("ok", true)) }
        "/api/delete" -> { requireMethod(session, Method.POST); val b = bodyJson(session); storage.delete(b.getString("path")); json(Response.Status.OK, JSONObject().put("ok", true)) }
        "/api/upload" -> upload(session)
        "/api/download" -> download(session, token)
        else -> json(Response.Status.NOT_FOUND, JSONObject().put("error", "API inconnue"))
    }

    private fun login(session: IHTTPSession): Response {
        val token = auth.login(bodyJson(session).optString("pin")) ?: return json(Response.Status.UNAUTHORIZED, JSONObject().put("error", "PIN incorrect"))
        return json(Response.Status.OK, JSONObject().put("token", token))
    }

    private fun upload(session: IHTTPSession): Response {
        requireMethod(session, Method.POST)
        val files = HashMap<String, String>(); session.parseBody(files)
        val tempPath = files["file"] ?: throw IllegalArgumentException("Fichier manquant")
        val originalName = session.parameters["file"]?.firstOrNull() ?: session.headers["x-file-name"] ?: throw IllegalArgumentException("Nom du fichier manquant")
        storage.importTempFile(session.parameters["path"]?.firstOrNull() ?: "", originalName, File(tempPath), session.headers["x-file-type"])
        return json(Response.Status.OK, JSONObject().put("ok", true).put("name", originalName))
    }

    private fun download(session: IHTTPSession, token: String): Response {
        val doc = storage.resolve(param(session, "path")) ?: return json(Response.Status.NOT_FOUND, JSONObject().put("error", "Fichier introuvable"))
        if (!doc.isFile) return json(Response.Status.BAD_REQUEST, JSONObject().put("error", "Ce n'est pas un fichier"))
        val input = storage.openInput(doc) ?: return json(Response.Status.INTERNAL_ERROR, JSONObject().put("error", "Lecture impossible"))
        return newChunkedResponse(Response.Status.OK, doc.type ?: "application/octet-stream", input).apply {
            val safe = (doc.name ?: "download").replace("\"", "")
            val disposition = if (session.parameters["download"]?.firstOrNull() == "1") "attachment" else "inline"
            addHeader("Content-Disposition", "$disposition; filename=\"$safe\""); addHeader("Cache-Control", "private, max-age=60")
        }
    }

    private fun bearer(session: IHTTPSession): String? = session.headers["authorization"]?.takeIf { it.startsWith("Bearer ", true) }?.substring(7)?.trim()
    private fun param(session: IHTTPSession, key: String): String = session.parameters[key]?.firstOrNull() ?: ""
    private fun bodyJson(session: IHTTPSession): JSONObject { val files = HashMap<String, String>(); session.parseBody(files); return JSONObject(files["postData"] ?: "{}") }
    private fun requireMethod(session: IHTTPSession, method: Method) { if (session.method != method) throw IllegalArgumentException("Méthode HTTP invalide") }
    private fun json(status: Response.Status, obj: JSONObject): Response = newFixedLengthResponse(status, "application/json; charset=utf-8", obj.toString()).apply { addHeader("Cache-Control", "no-store") }
    private fun asset(path: String, mime: String): Response = newFixedLengthResponse(Response.Status.OK, mime, context.assets.open(path).bufferedReader().use { it.readText() }).apply { addHeader("Cache-Control", "no-store") }
}
