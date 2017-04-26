+++
title = "serving multiple content types"
tags = ["content negotiation"]
categories = ["fintrospect-core"]
intro = ""
+++

```scala


object Serving_Multiple_Content_Types_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.ContentTypes.{APPLICATION_JSON, APPLICATION_XML}
  import io.fintrospect.parameters.Path
  import io.fintrospect.util.StrictContentTypeNegotiation
  import io.fintrospect.{RouteModule, RouteSpec}

  def serveJson(name: String) = Service.mk[Request, Response] { req =>
    import io.fintrospect.formats.Argo.JsonFormat._
    import io.fintrospect.formats.Argo.ResponseBuilder._
    Ok(obj("field" -> string(name)))
  }

  def serveXml(name: String) = Service.mk[Request, Response] {
    import io.fintrospect.formats.Xml.ResponseBuilder._
    req =>
      Ok(<root>
        <field>
          {name}
        </field>
      </root>)
  }

  val route = RouteSpec()
    .at(Get) / Path.string("name") bindTo
      StrictContentTypeNegotiation(APPLICATION_XML -> serveXml, APPLICATION_JSON -> serveJson)

  ready(Http.serve(":9999", RouteModule(Root).withRoute(route).toService))
}
//curl -v -H"Accept: application/json" http://localhost:9999/David
//curl -v -H"Accept: application/xml" http://localhost:9999/David
```