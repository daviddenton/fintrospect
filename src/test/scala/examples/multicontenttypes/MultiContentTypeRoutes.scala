package examples.multicontenttypes

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import io.fintrospect.{ContentTypes, ModuleSpec, MultiContentType, RouteSpec}

/**
  * Shows how to add routes which can serve multiple content types. Basically, the Accept header is checked
  * against the list of supplied services and a match found. If there is no Accept header set in the request,
  * the first service in the list is used. This means that there is NO sophisticated content negotiation implemented,
  * although Wildcard Accept headers will also match the first supplied service mapping.
  */
object MultiContentTypeRoutes extends App {

  private val serveJson = Service.mk { (rq: Request) => import io.fintrospect.formats.json.Argo.JsonFormat._
 import io.fintrospect.formats.json.Argo.ResponseBuilder.implicits._
    Ok(obj("field" -> string("value")))
  }

  private val serveXml = Service.mk {
    import io.fintrospect.formats.Xml.ResponseBuilder.implicits._
    (rq: Request) =>
      Ok(<root>
        <field>value</field>
      </root>)
  }

  private val serve = MultiContentType(
    ContentTypes.APPLICATION_SVG_XML -> serveXml,
    ContentTypes.APPLICATION_JSON -> serveJson
  )

  val route = RouteSpec()
    .producing(ContentTypes.APPLICATION_XML, ContentTypes.APPLICATION_JSON)
    .at(Get) / "multi" bindTo serve

  Http.serve(":8080", new HttpFilter(Cors.UnsafePermissivePolicy)
    .andThen(ModuleSpec(Root).withRoute(route).toService))

  println("See the service description at: http://localhost:8080 . The route at /multi should match wildcard Accept headers set in a browser.")

  Thread.currentThread().join()

}
