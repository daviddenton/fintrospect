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

  private def render(mr: ModuleRoute): Field = mr.route.method.getName.toLowerCase -> obj(
    "httpMethod" -> string(mr.route.method.getName),
    "nickname" -> string(mr.route.description.name),
    "notes" -> mr.route.description.summary.map(string).getOrElse(nullNode()),
    "produces" -> array(mr.route.description.produces.map(m => string(m.value))),
    "consumes" -> array(mr.route.description.consumes.map(m => string(m.value))),
    "parameters" -> array(mr.route.allParams.map(render)),
    "errorResponses" -> array(mr.route.description.responses
      .filter(_.status.getCode > 399)
      .map(resp => obj("code" -> number(resp.status.getCode), "reason" -> string(resp.description))).toSeq)
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