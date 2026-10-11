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
    fun hasRoot(): Boolean = root() != null

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
        if (!parent.isDirectory) throw IllegalArgumentException("Parent invalide")
        if (parent.findFile(name) != null) throw IllegalArgumentException("Ce nom existe déjà")
        if (parent.createDirectory(name) == null) throw IllegalStateException("Création impossible")
    }

    fun rename(path: String, newName: String) {
        validateName(newName)
        if (normalize(path).isBlank()) throw IllegalArgumentException("Impossible de renommer la racine")
        val doc = resolve(path) ?: throw IllegalArgumentException("Élément introuvable")
        if (!doc.renameTo(newName)) throw IllegalStateException("Renommage impossible")
    }

    fun delete(path: String) {
        if (normalize(path).isBlank()) throw IllegalArgumentException("Impossible de supprimer la racine")
        val doc = resolve(path) ?: throw IllegalArgumentException("Élément introuvable")
        if (!doc.delete()) throw IllegalStateException("Suppression impossible")
    }

    fun importTempFile(parentPath: String, originalName: String, tempFile: File, mime: String?) {
        validateName(originalName)
        val parent = resolve(parentPath) ?: throw IllegalArgumentException("Dossier cible introuvable")
        if (!parent.isDirectory) throw IllegalArgumentException("Dossier cible invalide")
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
        var current = root() ?: return null
        val n = normalize(path)
        if (n.isBlank()) return current
        for (segment in n.split('/')) {
            if (segment.isBlank() || segment == "." || segment == "..") return null
            current = current.findFile(segment) ?: return null
        }
        return current
    }

    private fun root(): DocumentFile? {
        AppConfig.fileRoot(context)?.takeIf { it.isNotBlank() }?.let { direct ->
            val file = File(direct)
            if (file.exists() && file.isDirectory && file.canRead()) return DocumentFile.fromFile(file)
        }
        val value = AppConfig.treeUri(context) ?: return null
        return try { DocumentFile.fromTreeUri(context, Uri.parse(value)) } catch (_: Exception) { null }
    }

    private fun openOutput(doc: DocumentFile) = if (doc.uri.scheme.equals("file", true)) {
        val path = doc.uri.path ?: return null
        try { FileOutputStream(File(path), false) } catch (_: Exception) { null }
    } else {
        try { context.contentResolver.openOutputStream(doc.uri, "w") } catch (_: Exception) { null }
    }

    private fun normalize(path: String): String = path.trim().trim('/').replace("//", "/")
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
