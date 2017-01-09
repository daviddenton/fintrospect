package cookbook.core

// fintrospect-core
object Swagger_Auto_Docs_Example extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.Future
  import io.fintrospect.formats.Argo.JsonFormat._
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.{Body, Header, Path, Query}
  import io.fintrospect.renderers.ModuleRenderer
  import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
  import io.fintrospect.{ApiKey, Module, RouteModule, RouteSpec, ServerRoute}

  def svc1(id: Int): Service[Request, Response] = Service.mk[Request, Response] { req => Ok(s"hello $id!") }

  val securityFilter: Service[String, Boolean] = Service.mk[String, Boolean] { r => Future(r == "secret") }

  val route: ServerRoute[Request, Response] = RouteSpec("a short summary", "a longer description")
    .taking(Query.required.string("firstName", "this is your firstname"))
    .taking(Header.optional.localDate("birthdate", "format yyyy-mm-dd"))
    .body(Body.json("family description", array(obj("lastName" -> string("Jane")), obj("name" -> string("Jim")))))
    .at(Post) / Path.int("id", "custom identifier for this request") bindTo svc1

  val swagger2dot0Json: ModuleRenderer = Swagger2dot0Json(
    ApiInfo("My App", "1.0", "This is an extended description of the API's functions")
  )
  val module: Module = RouteModule(Root, swagger2dot0Json)
    .securedBy(ApiKey(Query.required.string("token"), securityFilter))
    .withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

// curl -v http://localhost:9999/david