package cookbook.json

import io.fintrospect.formats.Circe

// fintrospect-core
object RunMe extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.Circe.ResponseBuilder._
  import io.fintrospect.parameters.Body
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  val json = Body.json(None, null, Circe)

  val echo: Service[Request, Response] = Service.mk[Request, Response] { req => Ok(json <-- req) }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(json)
    .at(Post) bindTo echo

  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v -XPOST http://localhost:9999/ --data '{"name":"david"}'