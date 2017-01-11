package cookbook.core


// fintrospect-core
object Minimal_Hello_World_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.Request
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder.Ok
  import io.fintrospect.{RouteModule, RouteSpec}

  ready(Http.serve(":9999",
    RouteModule(Root)
      .withRoute(
        RouteSpec().at(Get) bindTo
          Service.mk { req: Request => Ok(s"hello world!") }
      ).toService
  ))

}
