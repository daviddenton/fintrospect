package io.fintrospect.renderers.swagger1dot1

import argo.jdom.JsonNode
import com.twitter.finagle.httpx.Response
import com.twitter.finagle.httpx.path.Path
import io.fintrospect.ServerRoute
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.formats.json.Argo.ResponseBuilder._
import io.fintrospect.parameters.Parameter
import io.fintrospect.renderers.{JsonBadRequestRenderer, ModuleRenderer}

import scala.collection.JavaConversions._

/**
 * ModuleRenderer that provides basic Swagger v1.1 support. No support for bodies or schemas.
 */
class Swagger1dot1Json extends ModuleRenderer {

  private def render(parameter: Parameter): JsonNode = obj(
    "name" -> string(parameter.name),
    "description" -> parameter.description.map(string).getOrElse(nullNode()),
    "paramType" -> string(parameter.where),
    "required" -> boolean(parameter.required),
    "dataType" -> string(parameter.paramType.name)
  )

  private def render(route: ServerRoute): Field = route.method.toString().toLowerCase -> {
    val allParams: Seq[Parameter] =
      route.pathParams.flatMap(identity) ++
      route.routeSpec.queryParams ++
      route.routeSpec.headerParams ++
      route.routeSpec.body.map(_.iterator).getOrElse(Nil)

    obj(
      "Method" -> string(route.method.toString()),
      "nickname" -> string(route.routeSpec.summary),
      "notes" -> route.routeSpec.description.map(string).getOrElse(nullNode()),
      "produces" -> array(route.routeSpec.produces.map(m => string(m.value))),
      "consumes" -> array(route.routeSpec.consumes.map(m => string(m.value))),
      "parameters" -> array(allParams.map(render)),
      "errorResponses" -> array(route.routeSpec.responses
        .filter(_.status.code > 399)
        .map(resp => obj("code" -> number(resp.status.code), "reason" -> string(resp.description))))
    )
  }

  override def description(basePath: Path, routes: Seq[ServerRoute]): Response = {
    val api = routes
      .groupBy(_.describeFor(basePath))
      .map { case (path, routesForPath) => obj("path" -> string(path), "operations" -> array(routesForPath.map(render(_)._2))) }

    OK(obj("swaggerVersion" -> string("1.1"), "resourcePath" -> string("/"), "apis" -> array(asJavaIterable(api))))
  }

  override def badRequest(badParameters: Seq[Parameter]): Response = JsonBadRequestRenderer(badParameters)
}

object Swagger1dot1Json {
  def apply() = new Swagger1dot1Json()
}