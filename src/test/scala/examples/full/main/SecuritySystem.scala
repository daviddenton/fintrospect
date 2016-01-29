package examples.full.main

import java.net.URL
import java.time.Clock

import com.twitter.finagle._
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.util.Future
import io.fintrospect.formats.Html
import io.fintrospect.renderers.SiteMapModuleRenderer
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.templating.{RenderMustacheView, View}
import io.fintrospect.{Module, ModuleSpec, StaticModule}

class SecuritySystem(serverPort: Int, userDirectoryPort: Int, entryLoggerPort: Int, clock: Clock) {

  private var server: ListeningServer = null

  private val userDirectory = new UserDirectory(s"localhost:$userDirectoryPort")
  private val entryLogger = new EntryLogger(s"localhost:$entryLoggerPort", clock)
  private val inhabitants = new Inhabitants

  private val serviceModule = ModuleSpec(Root / "security",
    Swagger2dot0Json(ApiInfo("Security System", "1.0", Option("Building security system"))),
    new RequestCountingFilter(System.out)
  )
    .withDescriptionPath(_ / "api-docs")
    .securedBy(SecuritySystemAuth())
    .withRoutes(new KnockKnock(inhabitants, userDirectory, entryLogger))
    .withRoutes(new ByeBye(inhabitants, entryLogger))

  private val internalModule = ModuleSpec(Root / "internal", SimpleJson()).withRoute(new Ping().route)

  private val webModule = ModuleSpec[View](Root,
    new SiteMapModuleRenderer(new URL("http://my.security.system")),
    new RenderMustacheView(Html.ResponseBuilder, "examples/full/main/resources/templates")
  )
    .withDescriptionPath(_ / "sitemap.xml")
    .withRoutes(new ShowKnownUsers(userDirectory))
    .withRoutes(new ShowIndex(userDirectory))

  private val publicModule = StaticModule(Root, "examples/full/main/resources/public")

  // use CORs settings that suit your particular use-case. This one allows any cross-domain traffic at all and is applied
  // to all routes in the system
  private val globalFilter = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(CatchAll)

  def start() = {
    server = Http.serve(s":$serverPort", globalFilter.andThen(Module.toService(
      Module.combine(serviceModule, internalModule, webModule, publicModule))))
    Future.Done
  }

  def stop() = server.close()

}
