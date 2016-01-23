package examples.full.main

import java.net.URL
import java.time.Clock

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Future
import io.fintrospect.ModuleSpec
import io.fintrospect.formats.Html
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.renderers.{RenderMustacheView, SiteMapModuleRenderer}

class SecuritySystem(serverPort: Int, userDirectoryPort: Int, entryLoggerPort: Int, clock: Clock) {

  private var server: ListeningServer = null
  private val apiInfo = ApiInfo("Security System", "1.0", Option("Building security system"))
  private val userDirectory = new UserDirectory(s"localhost:$userDirectoryPort")
  private val entryLogger = new EntryLogger(s"localhost:$entryLoggerPort", clock)

  // use CORs settings that suit your particular use-case. This one allows any cross-domain traffic at all and is applied
  // to all routes in the system
  private val globalFilter = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(CatchAll)

  private val inhabitants = new Inhabitants

  private val serviceModule = ModuleSpec(Root / "security", Swagger2dot0Json(apiInfo))
    .securedBy(SecuritySystemAuth())
    .withDescriptionPath(_ / "api-docs")
    .withRoutes(new KnockKnock(inhabitants, userDirectory, entryLogger))
    .withRoutes(new ByeBye(inhabitants, entryLogger))

  private val webModule = ModuleSpec(Root / "web",
    new SiteMapModuleRenderer(new URL("http://my.security.system")),
    new RenderMustacheView(Html.ResponseBuilder)
  ).withRoutes(new ShowKnownUsers(userDirectory))

  private val statusModule = ModuleSpec(Root / "internal", SimpleJson()).withRoute(new Ping().route)

  def start() = {
    server = Http.serve(s":$serverPort", globalFilter.andThen(ModuleSpec.toService(ModuleSpec.combine(webModule, serviceModule, statusModule))))
    Future.Done
  }

  def stop() = server.close()

}
