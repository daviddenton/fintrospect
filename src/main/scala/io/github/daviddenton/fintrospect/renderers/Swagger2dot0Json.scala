package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories._
import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Parameter, Requirement}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

class Swagger2dot0Json private(apiInfo: ApiInfo) extends Renderer {

  private def render(rp: (Requirement, Parameter[_])): JsonNode = obj(
    "in" -> string(rp._2.where.toString),
    "name" -> string(rp._2.name),
    "description" -> rp._2.description.map(string).getOrElse(nullNode()),
    "required" -> booleanNode(rp._1.required),
    "type" -> string(rp._2.paramType)
  )

  private def render(r: ModuleRoute): (String, JsonNode) = {
    r.on.method.getName.toLowerCase -> obj(
      "tags" -> array(Seq(string(r.basePath.toString)): _*),
      "summary" -> r.description.summary.map(string).getOrElse(nullNode()),
      "produces" -> array(r.description.produces.map(m => string(m.value)): _*),
      "consumes" -> array(r.description.consumes.map(m => string(m.value)): _*),
      "parameters" -> array(r.allParams.map(render).toSeq: _*),
      "responses" -> obj(r.description.responses.map(resp => resp.status.getCode.toString -> obj("description" -> string(resp.description)))),
      "security" -> array(obj(Seq[Security]().map(_.toPathSecurity)))
    )
  }

  def apply(mr: Seq[ModuleRoute]): JsonRootNode = {
    val paths = mr
      .groupBy(_.toString)
      .map { case (path, routes) => path -> obj(routes.map(render))}.toSeq

    obj(
      "swagger" -> string("2.0"),
      "info" -> obj("title" -> string(apiInfo.title), "version" -> string(apiInfo.version), "description" -> string(apiInfo.description.getOrElse(""))),
      "basePath" -> string("/"),
      "paths" -> obj(paths)
      //    "definitions" -> obj(
      //      "User" -> obj(
      //        "properties" -> obj(
      //          "id" -> obj(
      //            "type" -> "integer",
      //            "format" -> "int64"
      //          )
      //        )
      //      )
      //    )
    )
  }
}


object Swagger2dot0Json {
  def apply(apiInfo: ApiInfo): Renderer = new Swagger2dot0Json(apiInfo)
}