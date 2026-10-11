package lu.guerrida.drivebox

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import lu.guerrida.drivebox.server.DriveBoxServerService
import java.io.File

class MainActivity : ComponentActivity() {
    private data class StorageChoice(val label: String, val path: String, val removable: Boolean)
    private var pendingDirectPath: String? = null

    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } catch (_: Exception) { }
            AppConfig.saveTreeUri(this, uri.toString())
            DriveBoxRuntime.lastError = null
        }
    }

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val legacyStoragePermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        val path = pendingDirectPath
        if (path != null && grants.values.all { it }) finishDirectSelection(path)
        else if (path != null) DriveBoxRuntime.lastError = "Autorisation de stockage refusée."
        pendingDirectPath = null
    }

    private val allFilesAccessLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val path = pendingDirectPath
        if (path != null && canUseDirectStorage(path)) finishDirectSelection(path)
        else if (path != null) DriveBoxRuntime.lastError = "Accès au disque non autorisé. Active 'Accès à tous les fichiers' pour DriveBox puis réessaie."
        pendingDirectPath = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { DriveBoxScreen() }
    }

    private fun systemFolderPickerAvailable(): Boolean {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        return try {
            intent.resolveActivity(packageManager) != null || packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
        } catch (_: Exception) { false }
    }

    private fun safeLaunchSystemFolderPicker() {
        if (!systemFolderPickerAvailable()) {
            DriveBoxRuntime.lastError = "Ce récepteur Android n'a pas de sélecteur de dossiers système. Utilise un volume USB/SSD détecté dans DriveBox."
            return
        }
        try {
            folderPicker.launch(null)
        } catch (_: ActivityNotFoundException) {
            DriveBoxRuntime.lastError = "Le sélecteur Android est absent. Choisis directement le volume USB/SSD."
        } catch (e: SecurityException) {
            DriveBoxRuntime.lastError = "Android a bloqué le sélecteur de stockage : ${e.message ?: "permission refusée"}."
        } catch (e: Exception) {
            DriveBoxRuntime.lastError = "Impossible d'ouvrir le sélecteur : ${e.message ?: e.javaClass.simpleName}."
        }
    }

    private fun storageChoices(): List<StorageChoice> {
        val sm = getSystemService(StorageManager::class.java)
        return sm.storageVolumes.mapNotNull { volume ->
            val state = volume.state
            if (state != Environment.MEDIA_MOUNTED && state != Environment.MEDIA_MOUNTED_READ_ONLY) return@mapNotNull null
            val dir = (if (Build.VERSION.SDK_INT >= 30) volume.directory else when {
                volume.isPrimary -> Environment.getExternalStorageDirectory()
                !volume.uuid.isNullOrBlank() -> File("/storage/${volume.uuid}")
                else -> null
            }) ?: return@mapNotNull null
            if (!dir.exists() || !dir.isDirectory) return@mapNotNull null
            val removable = volume.isRemovable
            val kind = if (removable) "USB / SSD" else "Stockage interne"
            val description = try { volume.getDescription(this).takeIf { it.isNotBlank() } } catch (_: Exception) { null }
            StorageChoice("${description ?: dir.name.ifBlank { kind }} — $kind", dir.absolutePath, removable)
        }.distinctBy { it.path }.sortedByDescending { it.removable }
    }

    private fun selectDirectStorage(path: String) {
        val root = File(path)
        if (!root.exists() || !root.isDirectory) {
            DriveBoxRuntime.lastError = "Le volume n'est plus disponible : $path"
            return
        }
        if (canUseDirectStorage(path)) {
            finishDirectSelection(path)
            return
        }
        pendingDirectPath = path
        if (Build.VERSION.SDK_INT >= 30) requestAllFilesAccess()
        else legacyStoragePermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun canUseDirectStorage(path: String): Boolean {
        val root = File(path)
        if (!root.exists() || !root.isDirectory || !root.canRead()) return false
        return if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager() || root.canWrite()
        else checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT < 30) return
        val intents = listOf(
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply { data = Uri.parse("package:$packageName") },
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        )
        for (intent in intents) {
            try {
                if (intent.resolveActivity(packageManager) != null) {
                    allFilesAccessLauncher.launch(intent)
                    return
                }
            } catch (_: Exception) { }
        }
        val path = pendingDirectPath
        if (path != null && File(path).canRead()) {
            finishDirectSelection(path)
            pendingDirectPath = null
        } else {
            DriveBoxRuntime.lastError = "Ce récepteur ne fournit pas l'écran d'autorisation fichiers. Vérifie Paramètres > Applications > DriveBox > Fichiers et médias."
        }
    }

    private fun finishDirectSelection(path: String) {
        val root = File(path)
        if (!root.exists() || !root.isDirectory || !root.canRead()) {
            DriveBoxRuntime.lastError = "DriveBox ne peut pas lire ce volume : $path"
            return
        }
        AppConfig.saveFileRoot(this, root.absolutePath)
        DriveBoxRuntime.lastError = null
    }

    @Composable
    private fun DriveBoxScreen() {
        var running by remember { mutableStateOf(DriveBoxRuntime.running) }
        var ip by remember { mutableStateOf(NetworkUtils.localIp()) }
        var storageLabel by remember { mutableStateOf(AppConfig.storageLabel(this)) }
        var showStorageDialog by remember { mutableStateOf(false) }
        var runtimeError by remember { mutableStateOf(DriveBoxRuntime.lastError) }
        val url = "http://$ip:${AppConfig.PORT}"
        val qr = remember(url) { QrCodeGenerator.generate(url, 420) }

        LaunchedEffect(Unit) {
            while (true) {
                running = DriveBoxRuntime.running
                ip = NetworkUtils.localIp()
                storageLabel = AppConfig.storageLabel(this@MainActivity)
                runtimeError = DriveBoxRuntime.lastError
                delay(700)
            }
        }

        MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF4F8CFF), background = Color(0xFF08111F), surface = Color(0xFF101C2E), onBackground = Color(0xFFF4F7FB), onSurface = Color(0xFFF4F7FB))) {
            if (showStorageDialog) {
                val choices = remember(showStorageDialog) { storageChoices() }
                AlertDialog(
                    onDismissRequest = { showStorageDialog = false },
                    title = { Text("Choisir le stockage") },
                    text = {
                        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("DriveBox peut utiliser le sélecteur Android ou choisir directement un disque monté.")
                            if (systemFolderPickerAvailable()) {
                                Button(onClick = { showStorageDialog = false; safeLaunchSystemFolderPicker() }, modifier = Modifier.fillMaxWidth()) { Text("Sélecteur de dossier Android") }
                            } else {
                                Text("Sélecteur Android indisponible — mode direct activé.", color = Color(0xFFFBBF24))
                            }
                            Text("Volumes détectés", fontWeight = FontWeight.Bold)
                            if (choices.isEmpty()) Text("Aucun volume monté. Branche le disque USB/SSD puis rouvre cette fenêtre.")
                            choices.forEach { choice ->
                                OutlinedButton(onClick = { showStorageDialog = false; selectDirectStorage(choice.path) }, modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.fillMaxWidth()) {
                                        Text(choice.label, fontWeight = FontWeight.Bold)
                                        Text(choice.path, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { showStorageDialog = false }) { Text("Fermer") } }
                )
            }

            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                            Text("DB", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("DriveBox Family", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                            Text("Cloud familial privé • V1.3", color = Color(0xFFA9B8CD))
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2E))) {
                        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(12.dp).background(if (running) Color(0xFF34D399) else Color(0xFFF87171), RoundedCornerShape(99.dp)))
                                Spacer(Modifier.width(10.dp))
                                Text(if (running) "SERVEUR ACTIF" else "SERVEUR ARRÊTÉ", fontWeight = FontWeight.Bold)
                            }
                            Text("Adresse familiale : $url", fontSize = 20.sp)
                            Text("Accès : profils individuels protégés par mot de passe", color = Color(0xFF91B7FF))
                            Text(if (storageLabel.isNullOrBlank()) "Stockage : aucun dossier choisi" else "Stockage : $storageLabel", color = if (storageLabel.isNullOrBlank()) Color(0xFFFBBF24) else Color(0xFF6EE7B7))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { showStorageDialog = true }) { Text("Choisir stockage / USB / SSD") }
                        if (!running) Button(onClick = { DriveBoxServerService.start(this@MainActivity) }) { Text("Démarrer serveur") }
                        else Button(onClick = { DriveBoxServerService.stop(this@MainActivity) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4233B))) { Text("Arrêter serveur") }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(qr.asImageBitmap(), contentDescription = "QR DriveBox", modifier = Modifier.size(220.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Scanner pour ouvrir DriveBox Family", color = Color(0xFF08111F), fontWeight = FontWeight.Bold)
                            Text(url, color = Color(0xFF475569))
                        }
                    }

                    Text("1. Branche le disque USB/SSD.  2. Choisis le stockage.  3. Démarre le serveur.  4. Chaque membre choisit son profil et son mot de passe.", color = Color(0xFFA9B8CD), lineHeight = 22.sp)
                    runtimeError?.let { Text("Erreur : $it", color = Color(0xFFFCA5A5)) }
                }
            }
        }
    }
}
