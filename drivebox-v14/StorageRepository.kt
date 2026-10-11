package lu.guerrida.drivebox.server

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import lu.guerrida.drivebox.AppConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class StorageRepository(private val context: Context) {
    private data class RootInfo(val doc: DocumentFile, val mode: String, val label: String, val fallback: Boolean)

    fun hasRoot(): Boolean = rootInfo() != null

    fun status(): JSONObject {
        val configured = !AppConfig.fileRoot(context).isNullOrBlank() || !AppConfig.treeUri(context).isNullOrBlank()
        val info = rootInfo()
        return JSONObject()
            .put("ready", info != null)
            .put("configured", configured)
            .put("mode", info?.mode ?: "NONE")
            .put("label", info?.label ?: "Aucun stockage")
            .put("fallback", info?.fallback ?: false)
            .put("writable", info?.doc?.canWrite() ?: false)
    }

    fun list(path: String): JSONObject {
        val dir = resolve(path) ?: throw IllegalArgumentException("Dossier introuvable")
        if (!dir.isDirectory) throw IllegalArgumentException("Ce chemin n'est pas un dossier")
        val arr = JSONArray()
        dir.listFiles().sortedWith(compareByDescending<DocumentFile> { it.isDirectory }.thenBy { it.name?.lowercase() ?: "" }).forEach { f ->
            val name = f.name ?: "Sans nom"
            arr.put(JSONObject().apply {
                put("name", name)
                put("path", join(path, name))
                put("directory", f.isDirectory)
                put("size", if (f.isFile) f.length() else 0)
                put("mime", f.type ?: guessMime(name))
                put("modified", f.lastModified())
            })
        }
        return JSONObject().put("path", normalize(path)).put("items", arr)
    }

    fun createFolder(parentPath: String, name: String) {
        validateName(name)
        val parent = resolve(parentPath) ?: throw IllegalArgumentException("Dossier parent introuvable")
        require(parent.isDirectory) { "Parent invalide" }
        require(parent.canWrite()) { "Stockage en lecture seule" }
        if (parent.findFile(name) != null) throw IllegalArgumentException("Ce nom existe déjà")
        if (parent.createDirectory(name) == null) throw IllegalStateException("Création impossible sur ce stockage")
    }

    fun rename(path: String, newName: String) {
        validateName(newName)
        if (normalize(path).isBlank()) throw IllegalArgumentException("Impossible de renommer la racine")
        val doc = resolve(path) ?: throw IllegalArgumentException("Élément introuvable")
        require(doc.canWrite()) { "Stockage en lecture seule" }
        if (!doc.renameTo(newName)) throw IllegalStateException("Renommage impossible")
    }

    fun delete(path: String) {
        if (normalize(path).isBlank()) throw IllegalArgumentException("Impossible de supprimer la racine")
        val doc = resolve(path) ?: throw IllegalArgumentException("Élément introuvable")
        require(doc.canWrite()) { "Stockage en lecture seule" }
        if (!doc.delete()) throw IllegalStateException("Suppression impossible")
    }

    fun importTempFile(parentPath: String, originalName: String, tempFile: File, mime: String?) {
        validateName(originalName)
        val parent = resolve(parentPath) ?: throw IllegalArgumentException("Dossier cible introuvable")
        require(parent.isDirectory) { "Dossier cible invalide" }
        require(parent.canWrite()) { "Stockage en lecture seule" }
        parent.findFile(originalName)?.delete()
        val type = mime?.takeIf { it.isNotBlank() } ?: guessMime(originalName)
        val target = parent.createFile(type, originalName) ?: throw IllegalStateException("Impossible de créer le fichier")
        openOutput(target).use { out ->
            if (out == null) throw IllegalStateException("Impossible d'écrire le fichier")
            FileInputStream(tempFile).use { input -> input.copyTo(out, 1024 * 1024) }
        }
    }

    fun openInput(doc: DocumentFile): InputStream? = if (doc.uri.scheme.equals("file", true)) {
        val path = doc.uri.path ?: return null
        try { FileInputStream(File(path)) } catch (_: Exception) { null }
    } else {
        try { context.contentResolver.openInputStream(doc.uri) } catch (_: Exception) { null }
    }

    fun resolve(path: String): DocumentFile? {
        var current = rootInfo()?.doc ?: return null
        val n = normalize(path)
        if (n.isBlank()) return current
        for (segment in n.split('/')) {
            if (segment.isBlank() || segment == "." || segment == "..") return null
            current = current.findFile(segment) ?: return null
        }
        return current
    }

    private fun rootInfo(): RootInfo? {
        AppConfig.fileRoot(context)?.takeIf { it.isNotBlank() }?.let { direct ->
            val file = File(direct)
            if (file.exists() && file.isDirectory && file.canRead() && file.canWrite()) {
                return RootInfo(DocumentFile.fromFile(file), "USB_DIRECT", file.absolutePath, false)
            }
        }

        AppConfig.treeUri(context)?.takeIf { it.isNotBlank() }?.let { value ->
            try {
                val doc = DocumentFile.fromTreeUri(context, Uri.parse(value))
                if (doc != null && doc.exists() && doc.isDirectory && doc.canRead() && doc.canWrite()) {
                    return RootInfo(doc, "ANDROID_SAF", "Dossier Android sélectionné", false)
                }
            } catch (_: Exception) { }
        }

        val fallbackBase = (context.getExternalFilesDir(null) ?: context.filesDir)
        val fallback = File(fallbackBase, "DriveBoxData")
        if (!fallback.exists()) fallback.mkdirs()
        if (fallback.exists() && fallback.isDirectory && fallback.canRead() && fallback.canWrite()) {
            return RootInfo(DocumentFile.fromFile(fallback), "INTERNAL_FALLBACK", "Stockage interne DriveBox", true)
        }
        return null
    }

    private fun openOutput(doc: DocumentFile) = if (doc.uri.scheme.equals("file", true)) {
        val path = doc.uri.path ?: return null
        try { FileOutputStream(File(path), false) } catch (_: Exception) { null }
    } else {
        try { context.contentResolver.openOutputStream(doc.uri, "w") } catch (_: Exception) { null }
    }

    private fun normalize(path: String): String = path.trim().trim('/').replace(Regex("/+"), "/")
    private fun join(parent: String, name: String): String = if (normalize(parent).isBlank()) name else normalize(parent) + "/" + name

    private fun validateName(name: String) {
        val n = name.trim()
        if (n.isBlank() || n == "." || n == ".." || n.contains('/') || n.contains('\\')) throw IllegalArgumentException("Nom invalide")
    }

    private fun guessMime(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }
}
