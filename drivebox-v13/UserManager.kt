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

    @Synchronized
    fun profiles(): List<Member> = load().filter { it.active && it.hash.isNotBlank() }.sortedBy { it.name.lowercase(Locale.ROOT) }

    @Synchronized
    fun findById(id: String): Member? = load().firstOrNull { it.id == id }

    @Synchronized
    fun create(displayName: String, password: String): Member {
        val name = validName(displayName)
        validPassword(password)
        val all = load().toMutableList()
        require(all.none { it.active && it.name.equals(name, true) }) { "Ce membre existe déjà" }

        val base = slug(name).ifBlank { "membre" }
        var username = base
        var index = 2
        while (all.any { it.username == username }) {
            username = "$base$index"
            index++
        }

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
    fun verifyById(id: String, password: String): Member? {
        val member = findById(id) ?: return null
        if (!member.active || member.salt.isBlank() || member.hash.isBlank()) return null
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
        val i = all.indexOfFirst { it.id == id }
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
