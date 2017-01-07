package cookbook.helloworld

// fintrospect-core
object RunMe extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.Path
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  def sayHello(name: String): Service[Request, Response] = Service.mk[Request, Response] { req => Ok(s"hello $name!") }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) / Path.string("name") bindTo sayHello
  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

// curl -v http://localhost:9999/david