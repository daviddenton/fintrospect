package cookbook.core

object Accepting_Multiple_Body_Types_Example extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.Request
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.parameters.Body
  import io.fintrospect.renderers.simplejson.SimpleJson
  import io.fintrospect.util.MultiBodyType
  import io.fintrospect.{RouteModule, RouteSpec}

  val json = Body.json()

  val echoJson = Service.mk { (rq: Request) =>
    import io.fintrospect.formats.Argo.ResponseBuilder._
    Ok(json <-- rq)
  }

  val xml = Body.xml()

  val echoXml = Service.mk { (rq: Request) =>
    import io.fintrospect.formats.Xml.ResponseBuilder._
    Ok(xml <-- rq)
  }

  val route = RouteSpec("echo posted content in either JSON or XML")
    .at(Post) bindTo MultiBodyType(json -> echoJson, xml -> echoXml)

  ready(Http.serve(":9999", RouteModule(Root, SimpleJson()).withRoute(route).toService))
}

//curl -v -XPOST -H"Content-Type: application/json" http://localhost:9999/ --data '{"name":"David"}'
//curl -v -XPOST -H"Content-Type: application/xml" http://localhost:9999/ --data '<name>David</name>'