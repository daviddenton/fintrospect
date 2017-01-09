package cookbook.core

// fintrospect-core
object Combining_Modules_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.Future
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.Query
  import io.fintrospect.{ApiKey, Module, RouteModule, RouteSpec, Security, ServerRoute}

  val identify: Service[Request, Response] = Service.mk[Request, Response] { req => Ok(req.uri) }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo identify
  val childModule: Module = RouteModule(Root / "child").withRoute(route)
  val rootModule: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", childModule.combine(rootModule).toService))
}

//curl -v http://localhost:9999/child
//curl -v http://localhost:9999
