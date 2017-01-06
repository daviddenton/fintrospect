package cookbook.xml

object RunMe extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.Xml.ResponseBuilder._
  import io.fintrospect.parameters.Body
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  import scala.xml.Elem

  val document = Body.xml(None)

  val analyse: Service[Request, Response] = Service.mk[Request, Response] {
    req => {
      val postedDoc: Elem = document <-- req
      Ok(
        <document>
          <number-of-root-elements>
            {postedDoc.length}
          </number-of-root-elements>
        </document>
      )
    }
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(document)
    .at(Post) bindTo analyse

  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
