package lu.guerrida.drivebox.server

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class AuthPrincipal(
    val memberId: String,
    val displayName: String,
    val username: String,
    val storageKey: String
)

class AuthManager(private val users: UserManager) {
    private data class Session(val principal: AuthPrincipal, val expiresAt: Long)
    private val sessions = ConcurrentHashMap<String, Session>()
    private val ttl = 24L * 60L * 60L * 1000L

    fun loginMember(memberId: String, password: String): String? {
        val member = users.verifyById(memberId, password) ?: return null
        return newSession(
            AuthPrincipal(
                memberId = member.id,
                displayName = member.name,
                username = member.username,
                storageKey = member.id
            )
        )
    }

    fun principal(token: String?): AuthPrincipal? {
        if (token.isNullOrBlank()) return null
        val session = sessions[token] ?: return null
        if (session.expiresAt < System.currentTimeMillis()) {
            sessions.remove(token)
            return null
        }
        return session.principal
    }

    fun logout(token: String?) {
        if (!token.isNullOrBlank()) sessions.remove(token)
    }

    fun invalidateMember(memberId: String) {
        sessions.entries.removeIf { it.value.principal.memberId == memberId }
    }

    private fun newSession(principal: AuthPrincipal): String {
        val now = System.currentTimeMillis()
        sessions.entries.removeIf { it.value.expiresAt < now }
        val token = UUID.randomUUID().toString().replace("-", "")
        sessions[token] = Session(principal, now + ttl)
        return token
    }
}
