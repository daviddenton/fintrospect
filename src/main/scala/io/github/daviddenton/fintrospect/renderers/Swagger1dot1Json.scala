package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Parameter
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.collection.JavaConversions._

object Swagger1dot1Json {

  private def render(p: Parameter[_]): JsonNode = obj(
    "name" -> string(p.name),
    "paramType" -> string(p.where.toString),
    "required" -> booleanNode(p.required.required),
    "dataType" -> string(p.paramType)
  )

  private def render(r: ModuleRoute): (String, JsonNode) = {
    r.on.method.getName.toLowerCase -> obj(
      "httpMethod" -> string(r.on.method.getName),
      "nickname" -> string(r.description.value),
      "summary" -> string(r.description.value),
      "produces" -> array(string("application/json")),
      "parameters" -> array(r.allParams.map(render): _*),
      "errorResponses" -> array(r.allResponses.filterKeys(_.getCode > 399).map { case (code, desc) => obj("code" -> number(code.getCode), "description" -> string(desc))}.toSeq)
    )
  }

  def apply(): Renderer =
    mr => {
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
