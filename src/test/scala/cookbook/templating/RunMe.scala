package cookbook.templating

// fintrospect-core
// fintrospect-mustache
object RunMe extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.Html
  import io.fintrospect.parameters.Path
  import io.fintrospect.templating.{MustacheTemplates, RenderView, View}
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  case class MyView(name: String, age: Int) extends View

  def showAgeIn30(name: String, age: Int): Service[Request, Response] = {
    val svc = Service.mk[Request, View] { req => MyView(name, age + 30) }

    new RenderView(Html.ResponseBuilder, MustacheTemplates.HotReload("src/main/resources")).andThen(svc)
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .at(Get) / Path.string("name") / Path.int("age") bindTo showAgeIn30

  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
