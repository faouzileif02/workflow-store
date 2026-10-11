package lu.guerrida.drivebox.server

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserManager(context: Context) {
    data class Member(
        val id: String,
        val username: String,
        val name: String,
        val salt: String,
        val hash: String,
        val codeSalt: String = "",
        val codeHash: String = "",
        val active: Boolean = true
    )

    private val prefs = context.getSharedPreferences("drivebox_family", Context.MODE_PRIVATE)
    private val random = SecureRandom()
    private val defaultProfiles = listOf("Faouzi", "Cynthia", "Amira", "Ines")

    init {
        migrateSeededPasswordsV132()
    }

    @Synchronized
    fun profiles(): List<Member> {
        seedDefaults()
        val order = defaultProfiles.mapIndexed { index, name -> name.lowercase(Locale.ROOT) to index }.toMap()
        return load().filter { it.active }.sortedWith(
            compareBy<Member> { order[it.name.lowercase(Locale.ROOT)] ?: 1000 }
                .thenBy { it.name.lowercase(Locale.ROOT) }
        )
    }

    @Synchronized
    fun findById(id: String): Member? {
        seedDefaults()
        return load().firstOrNull { it.id == id && it.active }
    }

    @Synchronized
    fun create(displayName: String, password: String): Member {
        val name = validName(displayName)
        validPassword(password)
        val all = load().toMutableList()
        require(all.none { it.active && it.name.equals(name, true) }) { "Ce membre existe déjà" }

        val username = uniqueUsername(name, all)
        val salt = randomBytes()
        val member = Member(
            id = UUID.randomUUID().toString().replace("-", ""),
            username = username,
            name = name,
            salt = encode(salt),
            hash = encode(kdf(password, salt, 180000)),
            active = true
        )
        all += member
        save(all)
        return member
    }

    @Synchronized
    fun setupPassword(id: String, password: String): Member {
        validPassword(password)
        val all = load().toMutableList()
        val i = all.indexOfFirst { it.id == id && it.active }
        require(i >= 0) { "Profil introuvable" }
        require(all[i].hash.isBlank()) { "Ce profil possède déjà un mot de passe" }
        val salt = randomBytes()
        all[i] = all[i].copy(
            salt = encode(salt),
            hash = encode(kdf(password, salt, 180000)),
            codeSalt = "",
            codeHash = "",
            active = true
        )
        save(all)
        return all[i]
    }

    @Synchronized
    fun verifyById(id: String, password: String): Member? {
        val member = findById(id) ?: return null
        if (!member.active) return null

        if (member.hash.isBlank() || member.salt.isBlank()) {
            return try {
                setupPassword(id, password)
            } catch (_: Exception) {
                null
            }
        }

        return try {
            if (MessageDigest.isEqual(kdf(password, decode(member.salt), 180000), decode(member.hash))) member else null
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    fun changePassword(id: String, currentPassword: String, newPassword: String) {
        validPassword(newPassword)
        val all = load().toMutableList()
        val i = all.indexOfFirst { it.id == id && it.active }
        require(i >= 0) { "Compte introuvable" }
        require(verifyById(id, currentPassword) != null) { "Mot de passe actuel incorrect" }
        val salt = randomBytes()
        all[i] = all[i].copy(salt = encode(salt), hash = encode(kdf(newPassword, salt, 180000)), active = true)
        save(all)
    }

    fun publicJson(member: Member): JSONObject = JSONObject()
        .put("id", member.id)
        .put("displayName", member.name)
        .put("username", member.username)
        .put("initial", member.name.trim().take(1).uppercase(Locale.ROOT))
        .put("configured", member.hash.isNotBlank())

    private fun migrateSeededPasswordsV132() {
        val migrationKey = "v132_seed_password_reset_done"
        if (prefs.getBoolean(migrationKey, false)) return

        val all = load().toMutableList()
        var changed = false
        for (i in all.indices) {
            if (defaultProfiles.any { it.equals(all[i].name, true) }) {
                all[i] = all[i].copy(
                    salt = "",
                    hash = "",
                    codeSalt = "",
                    codeHash = "",
                    active = true
                )
                changed = true
            }
        }
        if (changed) save(all)
        prefs.edit().putBoolean(migrationKey, true).commit()
    }

    private fun seedDefaults() {
        val all = load().toMutableList()
        var changed = false
        for (name in defaultProfiles) {
            val existing = all.indexOfFirst { it.name.equals(name, true) }
            if (existing >= 0) {
                if (!all[existing].active) {
                    all[existing] = all[existing].copy(active = true, codeSalt = "", codeHash = "")
                    changed = true
                }
            } else {
                all += Member(
                    id = UUID.randomUUID().toString().replace("-", ""),
                    username = uniqueUsername(name, all),
                    name = name,
                    salt = "",
                    hash = "",
                    active = true
                )
                changed = true
            }
        }
        if (changed) save(all)
    }

    private fun uniqueUsername(name: String, all: List<Member>): String {
        val base = slug(name).ifBlank { "membre" }
        var username = base
        var index = 2
        while (all.any { it.username == username }) {
            username = "$base$index"
            index++
        }
        return username
    }

    private fun validName(value: String): String {
        val name = value.trim().replace(Regex("\\s+"), " ")
        require(name.length in 2..60) { "Nom: entre 2 et 60 caractères" }
        require(!name.contains('/') && !name.contains('\\')) { "Nom invalide" }
        return name
    }

    private fun validPassword(value: String) {
        require(value.length in 6..128) { "Mot de passe: minimum 6 caractères" }
    }

    private fun slug(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
        return normalized.take(28)
    }

    private fun randomBytes(): ByteArray = ByteArray(16).also(random::nextBytes)

    private fun kdf(value: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(value.toCharArray(), salt, iterations, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    private fun load(): List<Member> {
        return try {
            val array = JSONArray(prefs.getString("users", "[]"))
            (0 until array.length()).map { index ->
                val o = array.getJSONObject(index)
                Member(
                    id = o.getString("id"),
                    username = o.optString("username", "membre$index"),
                    name = o.optString("name", o.optString("username", "Membre")),
                    salt = o.optString("salt"),
                    hash = o.optString("hash"),
                    codeSalt = o.optString("codeSalt"),
                    codeHash = o.optString("codeHash"),
                    active = o.optBoolean("active", true)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun save(all: List<Member>) {
        val array = JSONArray()
        all.forEach { member ->
            array.put(
                JSONObject()
                    .put("id", member.id)
                    .put("username", member.username)
                    .put("name", member.name)
                    .put("salt", member.salt)
                    .put("hash", member.hash)
                    .put("codeSalt", member.codeSalt)
                    .put("codeHash", member.codeHash)
                    .put("active", member.active)
            )
        }
        prefs.edit().putString("users", array.toString()).commit()
    }
}
