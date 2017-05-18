package io.fintrospect.renderers.swagger1dot1

import argo.jdom.JsonNode
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.Argo.JsonFormat._
import io.fintrospect.formats.Argo.ResponseBuilder._
import io.fintrospect.parameters.HasParameters
import io.fintrospect.renderers.{JsonErrorResponseRenderer, ModuleRenderer}
import io.fintrospect.util.ExtractionError
import io.fintrospect.{Security, ServerRoute}

/**
  * ModuleRenderer that provides basic Swagger v1.1 support. No support for bodies or schemas.
  */
class Swagger1dot1Json extends ModuleRenderer {

  private def render(parameters: HasParameters): Iterable[JsonNode] = parameters.map(
    parameter =>
      obj(
        "name" -> string(parameter.name),
        "description" -> Option(parameter.description).map(string).getOrElse(nullNode()),
        "paramType" -> string(parameter.where),
        "required" -> boolean(parameter.required),
        "dataType" -> string(parameter.paramType.name)
      )
  )

  private def render(route: ServerRoute[_, _]): Field = route.method.toString().toLowerCase -> {
    val allParams =
      route.pathParams.flatten ++
        route.routeSpec.requestParams.flatMap(_.iterator).flatten ++
        route.routeSpec.body.map(_.iterator).getOrElse(Nil)

    obj(
      "Method" -> string(route.method.toString()),
      "nickname" -> string(route.routeSpec.summary),
      "notes" -> route.routeSpec.description.map(string).getOrElse(nullNode()),
      "produces" -> array(route.routeSpec.produces.map(m => string(m.value))),
      "consumes" -> array(route.routeSpec.consumes.map(m => string(m.value))),
      "parameters" -> array(allParams.flatMap(render)),
      "errorResponses" -> array(route.routeSpec.responses.values.filter(_.status.code > 399)
        .map(resp => obj("code" -> number(resp.status.code), "reason" -> string(resp.description))))
    )
  }

  override def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_, _]]): Response = {
    Ok(obj("swaggerVersion" -> string("1.1"), "resourcePath" -> string("/"), "apis" -> array(routes
      .groupBy(_.describeFor(basePath))
      .map { case (path, routesForPath) => obj("path" -> string(path), "operations" -> array(routesForPath.map(render(_)._2))) }
      .toSeq: _*)))
  }

  override def badRequest(badParameters: Seq[ExtractionError]): Response = JsonErrorResponseRenderer.badRequest(badParameters)

  override def notFound(request: Request): Response = JsonErrorResponseRenderer.notFound()
}

object Swagger1dot1Json {
  def apply() = new Swagger1dot1Json()
}