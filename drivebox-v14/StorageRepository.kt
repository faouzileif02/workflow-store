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
import java.util.UUID

class StorageRepository(private val context: Context) {
    private data class RootInfo(
        val doc: DocumentFile,
        val mode: String,
        val label: String,
        val fallback: Boolean
    )

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
            .put("writable", info?.let { rootIsWritable(it.doc) } ?: false)
    }

    fun list(path: String): JSONObject {
        val dir = resolve(path) ?: throw IllegalArgumentException("Dossier introuvable")
        if (!dir.isDirectory) throw IllegalArgumentException("Ce chemin n'est pas un dossier")
        val arr = JSONArray()
        dir.listFiles()
            .sortedWith(compareByDescending<DocumentFile> { it.isDirectory }.thenBy { it.name?.lowercase() ?: "" })
            .forEach { f ->
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
        if (parent.findFile(name) != null) throw IllegalArgumentException("Ce nom existe déjà")

        if (isFileDoc(parent)) {
            val parentFile = fileOf(parent) ?: throw IllegalStateException("Chemin stockage invalide")
            val target = File(parentFile, name.trim())
            if (!target.mkdir()) {
                throw IllegalStateException("Création impossible : Android refuse l'écriture dans ${parentFile.absolutePath}")
            }
        } else {
            require(parent.canWrite()) { "Stockage en lecture seule" }
            if (parent.createDirectory(name.trim()) == null) {
                throw IllegalStateException("Création impossible sur le stockage Android sélectionné")
            }
        }
    }

    fun rename(path: String, newName: String) {
        validateName(newName)
        if (normalize(path).isBlank()) throw IllegalArgumentException("Impossible de renommer la racine")
        val doc = resolve(path) ?: throw IllegalArgumentException("Élément introuvable")

        if (isFileDoc(doc)) {
            val source = fileOf(doc) ?: throw IllegalStateException("Chemin stockage invalide")
            val target = File(source.parentFile ?: throw IllegalStateException("Parent introuvable"), newName.trim())
            if (!source.renameTo(target)) throw IllegalStateException("Renommage impossible")
        } else {
            require(doc.canWrite()) { "Stockage en lecture seule" }
            if (!doc.renameTo(newName.trim())) throw IllegalStateException("Renommage impossible")
        }
    }

    fun delete(path: String) {
        if (normalize(path).isBlank()) throw IllegalArgumentException("Impossible de supprimer la racine")
        val doc = resolve(path) ?: throw IllegalArgumentException("Élément introuvable")

        if (isFileDoc(doc)) {
            val file = fileOf(doc) ?: throw IllegalStateException("Chemin stockage invalide")
            val ok = if (file.isDirectory) file.deleteRecursively() else file.delete()
            if (!ok) throw IllegalStateException("Suppression impossible")
        } else {
            require(doc.canWrite()) { "Stockage en lecture seule" }
            if (!doc.delete()) throw IllegalStateException("Suppression impossible")
        }
    }

    fun importTempFile(parentPath: String, originalName: String, tempFile: File, mime: String?) {
        validateName(originalName)
        val parent = resolve(parentPath) ?: throw IllegalArgumentException("Dossier cible introuvable")
        require(parent.isDirectory) { "Dossier cible invalide" }

        if (isFileDoc(parent)) {
            val parentFile = fileOf(parent) ?: throw IllegalStateException("Chemin stockage invalide")
            val target = File(parentFile, originalName.trim())
            try {
                FileInputStream(tempFile).use { input ->
                    FileOutputStream(target, false).use { out ->
                        input.copyTo(out, 1024 * 1024)
                        out.fd.sync()
                    }
                }
            } catch (e: Exception) {
                try { target.delete() } catch (_: Exception) { }
                throw IllegalStateException("Impossible d'écrire le fichier sur ${parentFile.absolutePath} : ${e.message ?: "écriture refusée"}")
            }
            if (!target.exists() || target.length() != tempFile.length()) {
                throw IllegalStateException("Copie du fichier incomplète")
            }
        } else {
            require(parent.canWrite()) { "Stockage en lecture seule" }
            parent.findFile(originalName)?.delete()
            val type = mime?.takeIf { it.isNotBlank() } ?: guessMime(originalName)
            val target = parent.createFile(type, originalName.trim())
                ?: throw IllegalStateException("Impossible de créer le fichier")
            openOutput(target).use { out ->
                if (out == null) throw IllegalStateException("Impossible d'écrire le fichier")
                FileInputStream(tempFile).use { input -> input.copyTo(out, 1024 * 1024) }
            }
        }
    }

    fun openInput(doc: DocumentFile): InputStream? = if (isFileDoc(doc)) {
        val file = fileOf(doc) ?: return null
        try { FileInputStream(file) } catch (_: Exception) { null }
    } else {
        try { context.contentResolver.openInputStream(doc.uri) } catch (_: Exception) { null }
    }

    fun resolve(path: String): DocumentFile? {
        var current = rootInfo()?.doc ?: return null
        val n = normalize(path)
        if (n.isBlank()) return current

        for (segment in n.split('/')) {
            if (segment.isBlank() || segment == "." || segment == "..") return null
            current = if (isFileDoc(current)) {
                val parent = fileOf(current) ?: return null
                val child = File(parent, segment)
                if (!child.exists()) return null
                DocumentFile.fromFile(child)
            } else {
                current.findFile(segment) ?: return null
            }
        }
        return current
    }

    private fun rootInfo(): RootInfo? {
        val configuredDirect = AppConfig.fileRoot(context)?.takeIf { it.isNotBlank() }
        if (configuredDirect != null) {
            val direct = File(configuredDirect)
            if (direct.exists() && direct.isDirectory && direct.canRead() && probeFileWrite(direct)) {
                return RootInfo(DocumentFile.fromFile(direct), "USB_DIRECT", direct.absolutePath, false)
            }

            // Android 11+ may block writes to the volume root while still allowing
            // the app-specific directory on the SAME USB/SSD. Prefer that before
            // falling back to the receiver's internal storage.
            externalAppDirForVolume(direct)?.let { appDir ->
                val data = File(appDir, "DriveBoxData")
                if ((data.exists() || data.mkdirs()) && probeFileWrite(data)) {
                    return RootInfo(
                        DocumentFile.fromFile(data),
                        "USB_APP_SCOPED",
                        "USB/SSD DriveBox — ${data.absolutePath}",
                        true
                    )
                }
            }
        }

        AppConfig.treeUri(context)?.takeIf { it.isNotBlank() }?.let { value ->
            try {
                val doc = DocumentFile.fromTreeUri(context, Uri.parse(value))
                if (doc != null && doc.exists() && doc.isDirectory && doc.canRead() && probeSafWrite(doc)) {
                    return RootInfo(doc, "ANDROID_SAF", "Dossier Android sélectionné", false)
                }
            } catch (_: Exception) { }
        }

        val fallbackBase = context.filesDir
        val fallback = File(fallbackBase, "DriveBoxData")
        if ((fallback.exists() || fallback.mkdirs()) && probeFileWrite(fallback)) {
            return RootInfo(DocumentFile.fromFile(fallback), "INTERNAL_FALLBACK", "Stockage interne DriveBox", true)
        }
        return null
    }

    private fun externalAppDirForVolume(volumeRoot: File): File? {
        val root = try { volumeRoot.canonicalPath.trimEnd(File.separatorChar) } catch (_: Exception) { volumeRoot.absolutePath.trimEnd(File.separatorChar) }
        return context.getExternalFilesDirs(null)
            .filterNotNull()
            .firstOrNull { dir ->
                val path = try { dir.canonicalPath } catch (_: Exception) { dir.absolutePath }
                path == root || path.startsWith(root + File.separator)
            }
    }

    private fun probeFileWrite(dir: File): Boolean {
        if (!dir.exists() || !dir.isDirectory || !dir.canRead()) return false
        val test = File(dir, ".drivebox_write_${UUID.randomUUID().toString().take(8)}.tmp")
        return try {
            FileOutputStream(test, false).use { out ->
                out.write(byteArrayOf(68, 66))
                out.flush()
                out.fd.sync()
            }
            val ok = test.exists() && test.length() == 2L
            test.delete()
            ok
        } catch (_: Exception) {
            try { test.delete() } catch (_: Exception) { }
            false
        }
    }

    private fun probeSafWrite(root: DocumentFile): Boolean {
        val name = ".drivebox_test_${UUID.randomUUID().toString().take(8)}"
        return try {
            val test = root.createDirectory(name) ?: return false
            val ok = test.exists() && test.isDirectory
            test.delete()
            ok
        } catch (_: Exception) {
            false
        }
    }

    private fun rootIsWritable(doc: DocumentFile): Boolean = if (isFileDoc(doc)) {
        fileOf(doc)?.let { probeFileWrite(it) } ?: false
    } else doc.canWrite()

    private fun isFileDoc(doc: DocumentFile): Boolean = doc.uri.scheme.equals("file", true)
    private fun fileOf(doc: DocumentFile): File? = doc.uri.path?.let(::File)

    private fun openOutput(doc: DocumentFile) = if (isFileDoc(doc)) {
        val file = fileOf(doc) ?: return null
        try { FileOutputStream(file, false) } catch (_: Exception) { null }
    } else {
        try { context.contentResolver.openOutputStream(doc.uri, "w") } catch (_: Exception) { null }
    }

    private fun normalize(path: String): String = path.trim().trim('/').replace(Regex("/+"), "/")
    private fun join(parent: String, name: String): String = if (normalize(parent).isBlank()) name else normalize(parent) + "/" + name

    private fun validateName(name: String) {
        val n = name.trim()
        if (n.isBlank() || n == "." || n == ".." || n.contains('/') || n.contains('\\')) {
            throw IllegalArgumentException("Nom invalide")
        }
    }

    private fun guessMime(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }
}
