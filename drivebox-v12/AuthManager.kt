package lu.guerrida.drivebox.server

import android.content.Context
import lu.guerrida.drivebox.AppConfig
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class AuthPrincipal(val username:String,val displayName:String,val admin:Boolean,val storageKey:String)

class AuthManager(private val context:Context,private val users:UserManager){
    private data class S(val p:AuthPrincipal,val expires:Long)
    private val sessions=ConcurrentHashMap<String,S>(); private val ttl=24L*60*60*1000
    fun loginAdmin(pin:String):String?=if(pin==AppConfig.ensurePin(context))newSession(AuthPrincipal("admin","Administrateur",true,"admin"))else null
    fun loginMember(username:String,password:String):String?{val m=users.verify(username,password)?:return null;return newSession(AuthPrincipal(m.username,m.name,false,m.id))}
    fun principal(token:String?):AuthPrincipal?{if(token.isNullOrBlank())return null;val s=sessions[token]?:return null;if(s.expires<System.currentTimeMillis()){sessions.remove(token);return null};return s.p}
    fun logout(token:String?){if(!token.isNullOrBlank())sessions.remove(token)}
    fun invalidate(username:String){sessions.entries.removeIf{it.value.p.username==username}}
    private fun newSession(p:AuthPrincipal):String{val now=System.currentTimeMillis();sessions.entries.removeIf{it.value.expires<now};val t=UUID.randomUUID().toString().replace("-","");sessions[t]=S(p,now+ttl);return t}
}