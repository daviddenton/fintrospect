package io.github.daviddenton.fintrospect.swagger.v2dot0

import java.beans.Introspector.decapitalize

import argo.jdom.JsonNodeFactories._
import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.ModuleRoute
import io.github.daviddenton.fintrospect.swagger.{Location, SwParameter, SwPathMethod, SwResponse}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

object Swagger2Renderer extends (Seq[ModuleRoute] => JsonRootNode) {
  private def render(p: SwParameter): JsonNode = obj(
    "in" -> string(p.location.toString),
    "name" -> string(p.name),
    "required" -> booleanNode(true),
    "type" -> string(p.paramType)
  )


  private def render(pm: SwPathMethod): (String, JsonNode) = {
    pm.method.getName.toLowerCase -> obj(
      "summary" -> string(pm.summary),
      "produces" -> array(string("application/json")),
      "parameters" -> array(pm.params.map(render): _*),
      "responses" -> obj(pm.responses.map(render).map(cd => cd._1.toString -> cd._2)),
      "security" -> array(obj(pm.securities.map(_.toPathSecurity)))
    )
  }

  private def render(r: SwResponse): (Int, JsonNode) = r.code -> obj("description" -> string(r.description))

  private def render(r: ModuleRoute): (String, JsonNode) = {
    val params = r.segmentMatchers
      .flatMap(_.argument)
      .map { case (name, clazz) => SwParameter(name, Location.path, decapitalize(clazz.getSimpleName))}

    render(SwPathMethod(r.description.method, r.description.value, params, Seq(SwResponse(200, "")), Seq()))
  }

  override def apply(mr: Seq[ModuleRoute]): JsonRootNode = {
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
