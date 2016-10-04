package examples.strictcontenttypes

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.fintrospect.ContentTypes.{APPLICATION_JSON, APPLICATION_XML}
import io.fintrospect.filters.RequestFilters
import io.fintrospect.parameters.Path
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.util.StrictContentTypeNegotiation
import io.fintrospect.{ModuleSpec, RouteSpec}


/**
  * Shows how to add routes which can serve multiple content types using strict content-type negotiation.
  * Basically, the Accept header is checked against the list of supplied services and a match found. If there is no
  * Accept header set in the request, the first service in the list is used. This means that there is NO sophisticated
  * content negotiation implemented, although Wildcard Accept headers is supported to match the first supplied mapping service.
  */
object StrictMultiContentTypeRoute extends App {

  private def serveJson(name: String) = Service.mk { (rq: Request) =>
    import io.fintrospect.formats.Argo.JsonFormat._
    import io.fintrospect.formats.Argo.ResponseBuilder.implicits._
    Ok(obj("field" -> string(name)))
  }

  private def serveXml(name: String) = Service.mk {
    import io.fintrospect.formats.Xml.ResponseBuilder.implicits._
    (rq: Request) =>
      Ok(<root>
        <field>
          {name}
        </field>
      </root>)
  }

  val route = RouteSpec()
    .producing(APPLICATION_XML, APPLICATION_JSON)
    .at(Get) / "multi" / Path.string("name") bindTo StrictContentTypeNegotiation(APPLICATION_XML -> serveXml, APPLICATION_JSON -> serveJson)

  val jsonOnlyRoute = RouteSpec()
    .producing(APPLICATION_JSON)
    .at(Get) / "json" / Path.string("name") bindTo ((s) => RequestFilters.StrictAccept(APPLICATION_JSON).andThen(serveJson(s)))

  println("See the service description at: http://localhost:8080 . The route at /multi should match wildcard Accept headers set in a browser.")

  Await.ready(
    Http.serve(":8080", new HttpFilter(Cors.UnsafePermissivePolicy)
      .andThen(ModuleSpec(Root, SimpleJson()).withRoute(route).toService))
  )
}
