+++
title = "auto-documentation with swagger"
tags = ["swagger", "openapi", "docs"]
categories = ["fintrospect-core"]
intro = ""
+++

```scala


object Swagger_Auto_Docs_Example extends App {

  import argo.jdom.JsonRootNode
  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.filter.Cors
  import com.twitter.finagle.http.filter.Cors.HttpFilter
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response, Status}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.Future
  import io.fintrospect.ContentTypes.APPLICATION_JSON
  import io.fintrospect.formats.Argo.JsonFormat._
  import io.fintrospect.formats.Argo.ResponseBuilder._
  import io.fintrospect.parameters.{Body, Header, Path, Query}
  import io.fintrospect.renderers.ModuleRenderer
  import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
  import io.fintrospect.{ApiKey, Module, RouteModule, RouteSpec, ServerRoute}

  def buildResponse(id: Int, sent: JsonRootNode) = obj("id" -> number(id), "sent" -> sent)

  val exampleBody: JsonRootNode = array(obj("lastName" -> string("Jane")), obj("name" -> string("Jim")))
  val exampleResponse: JsonRootNode = buildResponse(222, exampleBody)
  val sentDocument: Body[JsonRootNode] = Body.json("family description", exampleBody)

  def svc(id: Int): Service[Request, Response] = Service.mk[Request, Response] { req =>
    Ok(buildResponse(id, sentDocument <-- req))
  }

  val securityFilter: Service[String, Boolean] = Service.mk[String, Boolean] { r => Future(r == "secret") }

  val route: ServerRoute[Request, Response] = RouteSpec("a short summary", "a longer description")
    .taking(Query.required.string("firstName", "this is your firstname"))
    .taking(Header.optional.localDate("birthdate", "format yyyy-mm-dd"))
    .producing(APPLICATION_JSON)
    .consuming(APPLICATION_JSON)
    .returning(Status.Ok -> "Valid request accepted", exampleResponse)
    .body(sentDocument)
    .at(Post) / Path.int("id", "custom identifier for this request") bindTo svc

  val docsRenderer: ModuleRenderer = Swagger2dot0Json(
    ApiInfo("My App", "1.0", "This is an extended description of the API's functions")
  )

  val module: Module = RouteModule(Root, docsRenderer)
    .withDescriptionPath(moduleRoot => moduleRoot / "swagger.json")
    .securedBy(ApiKey(Query.required.string("token"), securityFilter))
    .withRoute(route)

  ready(Http.serve(":9999", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(module.toService)))
}

// curl -v http://localhost:9999/swagger.json

```