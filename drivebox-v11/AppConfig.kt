package lu.guerrida.drivebox

import android.content.Context
import java.security.SecureRandom

object AppConfig {
    const val PREFS = "drivebox_prefs"
    const val KEY_TREE_URI = "tree_uri"
    const val KEY_FILE_ROOT = "file_root"
    const val KEY_PIN = "admin_pin"
    const val PORT = 8080

    fun ensurePin(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var pin = prefs.getString(KEY_PIN, null)
        if (pin.isNullOrBlank()) {
            pin = (SecureRandom().nextInt(900000) + 100000).toString()
            prefs.edit().putString(KEY_PIN, pin).apply()
        }
        return pin
    }

    fun treeUri(context: Context): String? = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TREE_URI, null)
    fun fileRoot(context: Context): String? = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FILE_ROOT, null)

    fun saveTreeUri(context: Context, uri: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TREE_URI, uri).remove(KEY_FILE_ROOT).apply()
    }

    fun saveFileRoot(context: Context, path: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_FILE_ROOT, path).remove(KEY_TREE_URI).apply()
    }

    fun storageLabel(context: Context): String? = fileRoot(context)?.takeIf { it.isNotBlank() }
        ?: treeUri(context)?.takeIf { it.isNotBlank() }?.let { "Dossier Android sélectionné" }
}
