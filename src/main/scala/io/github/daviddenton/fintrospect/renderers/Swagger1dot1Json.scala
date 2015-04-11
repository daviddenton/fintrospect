package io.github.daviddenton.fintrospect.renderers

import argo.jdom.{JsonNode, JsonRootNode}
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

  private def render(r: ModuleRoute): Field = r.route.method.getName.toLowerCase -> obj(
    "httpMethod" -> string(r.route.method.getName),
    "nickname" -> string(r.description.name),
    "notes" -> r.description.summary.map(string).getOrElse(nullNode()),
    "produces" -> array(r.description.produces.map(m => string(m.value))),
    "consumes" -> array(r.description.consumes.map(m => string(m.value))),
    "parameters" -> array(r.allParams.map(render)),
    "errorResponses" -> array(r.description.responses
      .filter(_.status.getCode > 399)
      .map(r => obj("code" -> number(r.status.getCode), "reason" -> string(r.description))).toSeq)
  )

  def apply(mr: Seq[ModuleRoute]): JsonRootNode = {
    val api = mr
      .groupBy(_.toString)
      .map { case (path, routes) => obj("path" -> string(path), "operations" -> array(routes.map(render(_)._2)))}

    obj("swaggerVersion" -> string("1.1"), "resourcePath" -> string("/"), "apis" -> array(asJavaIterable(api)))
  }
}

/**
 * Renderer that provides basic Swagger v1.1 support. No support for bodies or schemas.
 */
object Swagger1dot1Json {
  def apply(): Renderer = new Swagger1dot1Json()
}