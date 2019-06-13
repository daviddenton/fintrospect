package cookbook.core


// fintrospect-core
object Simple_JSON_Example extends App {

  import java.time.ZonedDateTime

  import argo.jdom.JsonNode
  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.Argo.JsonFormat._
  import io.fintrospect.formats.Argo.ResponseBuilder._
  import io.fintrospect.parameters.Body
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val json: Body[JsonNode] = Body.json()

  val echo: Service[Request, Response] = Service.mk[Request, Response] { req =>
    val requestJson: JsonNode = json <-- req
    val responseJson: JsonNode = obj(
      "posted" -> requestJson,
      "time" -> string(ZonedDateTime.now().toString)
    )
    Ok(responseJson)
  }

  val route: ServerRoute[Request, Response] = RouteSpec().body(json).at(Post) bindTo echo

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v -XPOST http://localhost:9999/ --data '{"name":"David"}'