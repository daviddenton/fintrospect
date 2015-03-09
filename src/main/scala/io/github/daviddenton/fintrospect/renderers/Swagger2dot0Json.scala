package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories._
import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Parameter
import io.github.daviddenton.fintrospect.util.ArgoUtil._

object Swagger2dot0Json {

  private def render(p: Parameter[_]): JsonNode = obj(
    "in" -> string(p.where.toString),
    "name" -> string(p.name),
    "description" -> p.description.map(string).getOrElse(nullNode()),
    "required" -> booleanNode(p.required.required),
    "type" -> string(p.paramType)
  )

  private def render(r: ModuleRoute): (String, JsonNode) = {
    r.on.method.getName.toLowerCase -> obj(
      "summary" -> string(r.description.value),
      "produces" -> array(r.description.produces.map(string): _*),
      "consumes" -> array(r.description.consumes.map(string): _*),
      "parameters" -> array(r.allParams.map(render): _*),
      "responses" -> obj(r.allResponses.map { case (code, desc) => code.getCode.toString -> obj("description" -> string(desc))}),
      "security" -> array(obj(Seq[Security]().map(_.toPathSecurity)))
    )
  }

  def apply(): Seq[ModuleRoute] => JsonRootNode =
    mr => {
      val paths = mr
        .groupBy(_.toString)
        .map { case (path, routes) => path -> obj(routes.map(render))}.toSeq

      obj(
        "swagger" -> string("2.0"),
        "info" -> obj("title" -> string("title"), "version" -> string("version")),
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
