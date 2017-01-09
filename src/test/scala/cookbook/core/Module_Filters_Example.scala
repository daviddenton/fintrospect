package cookbook.core

// fintrospect-core
object Module_Filters_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Filter, Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.renderers.ModuleRenderer
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val headers: Service[Request, Response] = Service.mk[Request, Response] { req => Ok(req.uri) }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo headers

  val timingFilter = Filter.mk[Request, Response, Request, Response] {
    (req, next) =>
      val start = System.currentTimeMillis()
      next(req).map {
        resp => {
          val time = System.currentTimeMillis() - start
          resp.headerMap("performance") = s"${resp.contentString} took $time ms"
          resp
        }
      }
  }

  val module: Module = RouteModule(Root, ModuleRenderer.Default, timingFilter).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v http://localhost:9999
