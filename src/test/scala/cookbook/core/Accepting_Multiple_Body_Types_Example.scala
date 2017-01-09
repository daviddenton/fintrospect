package cookbook.core


object Accepting_Multiple_Body_Types_Example extends App {

  import argo.jdom.JsonRootNode
  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.parameters.Body
  import io.fintrospect.util.MultiBodyType
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  import scala.xml.Elem

  val json: Body[JsonRootNode] = Body.json()

  val echoJson: Service[Request, Response] = Service.mk { (rq: Request) =>
    import io.fintrospect.formats.Argo.ResponseBuilder._
    Ok(json <-- rq)
  }

  val xml: Body[Elem] = Body.xml()

  val echoXml: Service[Request, Response] = Service.mk { (rq: Request) =>
    import io.fintrospect.formats.Xml.ResponseBuilder._
    Ok(xml <-- rq)
  }

  val route: ServerRoute[Request, Response] = RouteSpec("echo posted content in either JSON or XML")
    .at(Post) bindTo MultiBodyType(json -> echoJson, xml -> echoXml)

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v -XPOST -H"Content-Type: application/json" http://localhost:9999/ --data '{"name":"David"}'
//curl -v -XPOST -H"Content-Type: application/xml" http://localhost:9999/ --data '<name>David</name>'