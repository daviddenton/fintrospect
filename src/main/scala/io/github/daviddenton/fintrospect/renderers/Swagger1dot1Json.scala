package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories._
import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Requirement, Parameter}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.collection.JavaConversions._

class Swagger1dot1Json private() extends Renderer {

  private def render(rp: (Requirement, Parameter[_])): JsonNode = obj(
    "name" -> string(rp._2.name),
    "description" -> rp._2.description.map(string).getOrElse(nullNode()),
    "paramType" -> string(rp._2.where.toString),
    "required" -> booleanNode(rp._1.required),
    "dataType" -> string(rp._2.paramType)
  )

  private def render(r: ModuleRoute): (String, JsonNode) = {
    r.on.method.getName.toLowerCase -> obj(
      "httpMethod" -> string(r.on.method.getName),
      "nickname" -> string(r.description.name),
      "notes" -> r.description.summary.map(string).getOrElse(nullNode()),
      "produces" -> array(r.description.produces.map(m => string(m.value)): _*),
      "consumes" -> array(r.description.consumes.map(m => string(m.value)): _*),
      "parameters" -> {
        array(r.allParams.map(render).toSeq: _*)
      },
      "errorResponses" -> array(r.allResponses.filterKeys(_.getCode > 399).map { case (code, desc) => obj("code" -> number(code.getCode), "reason" -> string(desc))}.toSeq)
    )
  }

  def apply(mr: Seq[ModuleRoute]): JsonRootNode = {
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

object Swagger1dot1Json {
  def apply(): Renderer = new Swagger1dot1Json()
}