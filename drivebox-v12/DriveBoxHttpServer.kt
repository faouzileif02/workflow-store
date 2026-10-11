package lu.guerrida.drivebox.server

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Method
import fi.iki.elonen.NanoHTTPD.Response
import lu.guerrida.drivebox.AppConfig
import lu.guerrida.drivebox.NetworkUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DriveBoxHttpServer(private val context:Context,port:Int=AppConfig.PORT):NanoHTTPD(port){
    private val users=UserManager(context); private val auth=AuthManager(context,users); private val storage=StorageRepository(context)
    private val familyRoot=".drivebox_family"

    override fun serve(s:IHTTPSession):Response=try{
        when{
            s.uri=="/"||s.uri=="/index.html"->asset("drivebox/index.html","text/html; charset=utf-8")
            s.uri=="/api/login"&&s.method==Method.POST->login(s)
            s.uri=="/api/activate"&&s.method==Method.POST->activate(s)
            s.uri.startsWith("/api/")-> {val token=bearer(s)?:s.parameters["token"]?.firstOrNull();val p=auth.principal(token);if(p==null)json(Response.Status.UNAUTHORIZED,JSONObject().put("error","Authentification requise"))else api(s,token?:"",p)}
            else->newFixedLengthResponse(Response.Status.NOT_FOUND,MIME_PLAINTEXT,"Not found")
        }
    }catch(e:Exception){json(Response.Status.BAD_REQUEST,JSONObject().put("error",e.message?:"Erreur"))}

    private fun api(s:IHTTPSession,token:String,p:AuthPrincipal):Response=when(s.uri){
        "/api/status"->json(Response.Status.OK,JSONObject().put("ok",true).put("name","DriveBox").put("version","1.2.0").put("ip",NetworkUtils.localIp()).put("port",AppConfig.PORT).put("storageReady",storage.hasRoot()).put("user",userJson(p)).put("membersCount",if(p.admin)users.list().size else 0))
        "/api/list"->json(Response.Status.OK,listFor(p,param(s,"path")))
        "/api/mkdir"->{method(s,Method.POST);val b=body(s);storage.createFolder(scope(p,b.optString("path","")),b.getString("name"));ok()}
        "/api/rename"->{method(s,Method.POST);val b=body(s);storage.rename(scope(p,b.getString("path")),b.getString("name"));ok()}
        "/api/delete"->{method(s,Method.POST);val b=body(s);storage.delete(scope(p,b.getString("path")));ok()}
        "/api/upload"->upload(s,p)
        "/api/download"->download(s,p)
        "/api/logout"->{method(s,Method.POST);auth.logout(token);ok()}
        "/api/family/members"->family(s,p)
        "/api/family/reset"->reset(s,p)
        else->json(Response.Status.NOT_FOUND,JSONObject().put("error","API inconnue"))
    }

    private fun login(s:IHTTPSession):Response{
        val b=body(s);val mode=b.optString("mode","member");val token=if(mode=="admin")auth.loginAdmin(b.optString("pin"))else auth.loginMember(b.optString("username"),b.optString("password"))
        if(token==null)return json(Response.Status.UNAUTHORIZED,JSONObject().put("error",if(mode=="admin")"PIN administrateur incorrect" else "Utilisateur ou mot de passe incorrect"))
        val p=auth.principal(token)!!; if(!p.admin)ensureMemberRoot(p);return json(Response.Status.OK,JSONObject().put("token",token).put("user",userJson(p)))
    }

    private fun activate(s:IHTTPSession):Response{
        val b=body(s);val pass=b.optString("password");require(pass==b.optString("confirm")){"Les mots de passe ne correspondent pas"};val m=users.activate(b.optString("username"),b.optString("code"),pass);ensureMemberRoot(AuthPrincipal(m.username,m.name,false,m.id));return json(Response.Status.OK,JSONObject().put("ok",true).put("message","Compte activé"))
    }

    private fun family(s:IHTTPSession,p:AuthPrincipal):Response{
        require(p.admin){"Accès administrateur requis"}
        return when(s.method){
            Method.GET->{val a=JSONArray();users.list().forEach{a.put(users.pub(it))};json(Response.Status.OK,JSONObject().put("members",a))}
            Method.POST->{val b=body(s);val c=users.create(b.optString("username"),b.optString("displayName"));ensureMemberRoot(AuthPrincipal(c.member.username,c.member.name,false,c.member.id));json(Response.Status.OK,JSONObject().put("member",users.pub(c.member)).put("activationCode",c.code))}
            else->throw IllegalArgumentException("Méthode HTTP invalide")
        }
    }

    private fun reset(s:IHTTPSession,p:AuthPrincipal):Response{require(p.admin){"Accès administrateur requis"};method(s,Method.POST);val u=body(s).optString("username");val code=users.reset(u);auth.invalidate(u.trim().lowercase());return json(Response.Status.OK,JSONObject().put("ok",true).put("activationCode",code))}

    private fun ensureMemberRoot(p:AuthPrincipal){if(!storage.hasRoot())return;if(storage.resolve(familyRoot)==null)storage.createFolder("",familyRoot);val base="$familyRoot/${p.storageKey}";if(storage.resolve(base)==null)storage.createFolder(familyRoot,p.storageKey)}

    private fun scope(p:AuthPrincipal,path:String):String{
        val n=path.trim().trim('/').replace(Regex("/+"),"/")
        if(p.admin){require(n!=familyRoot&&!n.startsWith("$familyRoot/")){"Dossier privé protégé"};return n}
        ensureMemberRoot(p);val pre="$familyRoot/${p.storageKey}";return if(n.isBlank())pre else "$pre/$n"
    }

    private fun listFor(p:AuthPrincipal,path:String):JSONObject{
        val d=storage.list(scope(p,path));val src=d.getJSONArray("items");val out=JSONArray();val pre="$familyRoot/${p.storageKey}/"
        for(i in 0 until src.length()){val o=src.getJSONObject(i);if(p.admin&&path.isBlank()&&o.optString("name")==familyRoot)continue;val copy=JSONObject(o.toString());if(!p.admin){val sp=copy.optString("path");copy.put("path",if(sp.startsWith(pre))sp.removePrefix(pre) else "")};out.put(copy)}
        return JSONObject().put("path",path.trim().trim('/')).put("items",out)
    }

    private fun upload(s:IHTTPSession,p:AuthPrincipal):Response{method(s,Method.POST);val files=HashMap<String,String>();s.parseBody(files);val tmp=files["file"]?:throw IllegalArgumentException("Fichier manquant");val name=s.parameters["file"]?.firstOrNull()?:s.headers["x-file-name"]?:throw IllegalArgumentException("Nom du fichier manquant");storage.importTempFile(scope(p,s.parameters["path"]?.firstOrNull()?:""),name,File(tmp),s.headers["x-file-type"]);return json(Response.Status.OK,JSONObject().put("ok",true).put("name",name))}

    private fun download(s:IHTTPSession,p:AuthPrincipal):Response{val doc=storage.resolve(scope(p,param(s,"path")))?:return json(Response.Status.NOT_FOUND,JSONObject().put("error","Fichier introuvable"));if(!doc.isFile)return json(Response.Status.BAD_REQUEST,JSONObject().put("error","Ce n'est pas un fichier"));val input=storage.openInput(doc)?:return json(Response.Status.INTERNAL_ERROR,JSONObject().put("error","Lecture impossible"));val r=newChunkedResponse(Response.Status.OK,doc.type?:"application/octet-stream",input);val safe=(doc.name?:"download").replace("\"","");r.addHeader("Content-Disposition",(if(s.parameters["download"]?.firstOrNull()=="1")"attachment" else "inline")+"; filename=\"$safe\"");r.addHeader("Cache-Control","private, max-age=60");return r}

    private fun userJson(p:AuthPrincipal)=JSONObject().put("username",p.username).put("displayName",p.displayName).put("admin",p.admin)
    private fun bearer(s:IHTTPSession)=s.headers["authorization"]?.takeIf{it.startsWith("Bearer ",true)}?.substring(7)?.trim()
    private fun param(s:IHTTPSession,k:String)=s.parameters[k]?.firstOrNull()?:""
    private fun body(s:IHTTPSession):JSONObject{val f=HashMap<String,String>();s.parseBody(f);return JSONObject(f["postData"]?:"{}")}
    private fun method(s:IHTTPSession,m:Method){if(s.method!=m)throw IllegalArgumentException("Méthode HTTP invalide")}
    private fun ok()=json(Response.Status.OK,JSONObject().put("ok",true))
    private fun json(st:Response.Status,o:JSONObject)=newFixedLengthResponse(st,"application/json; charset=utf-8",o.toString()).apply{addHeader("Cache-Control","no-store");addHeader("X-Content-Type-Options","nosniff")}
    private fun asset(path:String,mime:String)=newFixedLengthResponse(Response.Status.OK,mime,context.assets.open(path).bufferedReader().use{it.readText()}).apply{addHeader("Cache-Control","no-store")}
}