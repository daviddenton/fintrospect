package io.github.daviddenton.fintrospect.renderers

import java.beans.Introspector._

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.collection.JavaConversions._
import org.jboss.netty.handler.codec.http.HttpMethod

object Swagger1dot1Json {

  private case class PathMethod(method: HttpMethod, summary: String, params: Seq[Parameter], responses: Seq[PathResponse], securities: Seq[Security])

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
