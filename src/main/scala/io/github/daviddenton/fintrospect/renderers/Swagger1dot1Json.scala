package io.github.daviddenton.fintrospect.renderers

import argo.jdom.{JsonNode, JsonRootNode}
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Parameter, Requirement}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.collection.JavaConversions._

class Swagger1dot1Json private() extends Renderer {

  private def render(requirementAndParameter: (Requirement, Parameter[_])): JsonNode = obj(
    "name" -> string(requirementAndParameter._2.name),
    "description" -> requirementAndParameter._2.description.map(string).getOrElse(nullNode()),
    "paramType" -> string(requirementAndParameter._2.where.toString),
    "required" -> boolean(requirementAndParameter._1.required),
    "dataType" -> string(requirementAndParameter._2.paramType.name)
  )

  private def render(route: Route): Field = route.method.getName.toLowerCase -> obj(
    "httpMethod" -> string(route.method.getName),
    "nickname" -> string(route.describedRoute.summary),
    "notes" -> string(route.describedRoute.summary),
    "produces" -> array(route.describedRoute.produces.map(m => string(m.value))),
    "consumes" -> array(route.describedRoute.consumes.map(m => string(m.value))),
    "parameters" -> array(route.allParams.map(render)),
    "errorResponses" -> array(route.describedRoute.responses
      .filter(_.status.getCode > 399)
      .map(resp => obj("code" -> number(resp.status.getCode), "reason" -> string(resp.description))).toSeq)
  )

  def apply(basePath: Path, routes: Seq[Route]): JsonRootNode = {
    val api = routes
      .groupBy(_.describeFor(basePath))
      .map { case (path, routesForPath) => obj("path" -> string(path), "operations" -> array(routesForPath.map(render(_)._2)))}

    obj("swaggerVersion" -> string("1.1"), "resourcePath" -> string("/"), "apis" -> array(asJavaIterable(api)))
  }
}

/**
 * Renderer that provides basic Swagger v1.1 support. No support for bodies or schemas.
 */
object Swagger1dot1Json {
  def apply(): Renderer = new Swagger1dot1Json()
}
