package io.github.daviddenton.fintrospect.swagger.v1dot1

import java.beans.Introspector._

import argo.jdom.JsonNodeFactories._
import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.{Location, ModuleRoute}
import io.github.daviddenton.fintrospect.swagger.{Parameter, PathMethod}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.collection.JavaConversions._

object Swagger1Renderer extends (Seq[ModuleRoute] => JsonRootNode) {
  private def render(p: Parameter): JsonNode = obj(
    "name" -> string(p.name),
    "paramType" -> string(p.location.toString),
    "required" -> booleanNode(true),
    "dataType" -> string(p.paramType)
  )

  private def render(pm: PathMethod): (String, JsonNode) = pm.method.getName.toLowerCase -> obj(
    "httpMethod" -> string(pm.method.getName),
    "nickname" -> string(pm.summary),
    "summary" -> string(pm.summary),
    "produces" -> array(string("application/json")),
    "parameters" -> array(pm.params.map(render): _*),
    "errorResponses" -> {
      array(pm.responses.map(r => r.code -> string(r.description)).map(p => obj("code" -> number(p._1), "description" -> p._2)))
    }
  )

  private def render(r: ModuleRoute): (String, JsonNode) = {
    val params = r.segmentMatchers
      .flatMap(_.argument)
      .map { case (name, clazz) => Parameter(name, Location.path, decapitalize(clazz.getSimpleName))}

    render(PathMethod(r.description.method, r.description.value, params, Seq(), Seq()))
  }

  override def apply(mr: Seq[ModuleRoute]): JsonRootNode = {
    val api = mr
      .groupBy(_.toString)
      .map { case (path, routes) => obj("path" -> string(path), "operations" -> array(routes.map(render(_)._2): _*))}

    obj(
      "swaggerVersion" -> string("1.1"),
      "resourcePath" -> string("/"),
      "apis" -> array(asJavaIterable(api))
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
