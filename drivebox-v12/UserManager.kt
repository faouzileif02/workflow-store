package lu.guerrida.drivebox.server

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserManager(context: Context) {
    data class Member(val id:String,val username:String,val name:String,val salt:String,val hash:String,val codeSalt:String,val codeHash:String,val active:Boolean)
    data class Created(val member:Member,val code:String)
    private val prefs=context.getSharedPreferences("drivebox_family",Context.MODE_PRIVATE)
    private val rnd=SecureRandom()

    @Synchronized fun list():List<Member> = load().sortedBy{it.name.lowercase(Locale.ROOT)}
    @Synchronized fun find(username:String)=load().firstOrNull{it.username==norm(username)}

    @Synchronized fun create(username:String,name:String):Created{
        val u=validUser(username); val n=name.trim().ifBlank{u}; require(n.length<=60){"Nom trop long"}
        val all=load().toMutableList(); require(all.none{it.username==u}){"Ce nom d'utilisateur existe déjà"}
        val code=(rnd.nextInt(900000)+100000).toString(); val cs=random();
        val m=Member(UUID.randomUUID().toString().replace("-",""),u,n,"","",enc(cs),enc(kdf(code,cs,60000)),false)
        all+=m; save(all); return Created(m,code)
    }

    @Synchronized fun activate(username:String,code:String,password:String):Member{
        validPassword(password); val all=load().toMutableList(); val i=all.indexOfFirst{it.username==norm(username)}; require(i>=0){"Compte introuvable"}
        val m=all[i]; require(!m.active){"Compte déjà activé"}; require(m.codeSalt.isNotBlank()){"Code expiré"}
        require(MessageDigest.isEqual(kdf(code.trim(),dec(m.codeSalt),60000),dec(m.codeHash))){"Code d'activation incorrect"}
        val salt=random(); val out=m.copy(salt=enc(salt),hash=enc(kdf(password,salt,180000)),codeSalt="",codeHash="",active=true)
        all[i]=out; save(all); return out
    }

    @Synchronized fun verify(username:String,password:String):Member?{
        val m=find(username)?:return null; if(!m.active||m.salt.isBlank())return null
        return try{if(MessageDigest.isEqual(kdf(password,dec(m.salt),180000),dec(m.hash)))m else null}catch(_:Exception){null}
    }

    @Synchronized fun reset(username:String):String{
        val all=load().toMutableList(); val i=all.indexOfFirst{it.username==norm(username)}; require(i>=0){"Compte introuvable"}
        val code=(rnd.nextInt(900000)+100000).toString(); val cs=random(); all[i]=all[i].copy(salt="",hash="",codeSalt=enc(cs),codeHash=enc(kdf(code,cs,60000)),active=false); save(all); return code
    }

    fun pub(m:Member)=JSONObject().put("username",m.username).put("displayName",m.name).put("activated",m.active)

    private fun norm(v:String)=v.trim().lowercase(Locale.ROOT)
    private fun validUser(v:String):String{val u=norm(v);require(u.matches(Regex("[a-z0-9._-]{2,32}"))){"Utilisateur: 2-32 caractères, lettres/chiffres/._-"};require(u!="admin"&&u!="administrateur"){"Nom réservé"};return u}
    private fun validPassword(v:String){require(v.length in 6..128){"Mot de passe: minimum 6 caractères"}}
    private fun random()=ByteArray(16).also(rnd::nextBytes)
    private fun kdf(v:String,s:ByteArray,it:Int):ByteArray{val spec=PBEKeySpec(v.toCharArray(),s,it,256);return try{SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded}finally{spec.clearPassword()}}
    private fun enc(b:ByteArray)=Base64.encodeToString(b,Base64.NO_WRAP); private fun dec(s:String)=Base64.decode(s,Base64.NO_WRAP)
    private fun load():List<Member>{return try{val a=JSONArray(prefs.getString("users","[]"));(0 until a.length()).map{val o=a.getJSONObject(it);Member(o.getString("id"),o.getString("username"),o.optString("name",o.getString("username")),o.optString("salt"),o.optString("hash"),o.optString("codeSalt"),o.optString("codeHash"),o.optBoolean("active"))}}catch(_:Exception){emptyList()}}
    private fun save(all:List<Member>){val a=JSONArray();all.forEach{m->a.put(JSONObject().put("id",m.id).put("username",m.username).put("name",m.name).put("salt",m.salt).put("hash",m.hash).put("codeSalt",m.codeSalt).put("codeHash",m.codeHash).put("active",m.active))};prefs.edit().putString("users",a.toString()).commit()}
}