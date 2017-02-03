package cookbook.core

object HelloWorld_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val svc: Service[Request, Response] = Service.mk[Request, Response] { req => Ok(s"hello world!") }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo svc

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

// curl -v http://localhost:9999
