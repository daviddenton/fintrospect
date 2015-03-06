package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.FintrospectModule.Renderer
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpMethod

object Swagger2dot0Json {
  private case class PathMethod(method: HttpMethod, summary: String, params: Seq[Parameter], responses: Seq[PathResponse], securities: Seq[Security])

  private def render(p: Parameter): JsonNode = obj(
    "in" -> string(p.location.toString),
    "name" -> string(p.name),
    "required" -> booleanNode(true),
    "type" -> string(p.paramType)
  )


  private def render(pm: PathMethod): (String, JsonNode) = {
    pm.method.getName.toLowerCase -> obj(
      "summary" -> string(pm.summary),
      "produces" -> array(string("application/json")),
      "parameters" -> array(pm.params.map(render): _*),
      "responses" -> obj(pm.responses.map(r => r.code -> obj("description" -> string(r.description))).map(cd => cd._1.toString -> cd._2)),
      "security" -> array(obj(pm.securities.map(_.toPathSecurity)))
    )
  }

  private def render(r: ModuleRoute): (String, JsonNode) = {
    render(PathMethod(r.description.method, r.description.value, r.segmentMatchers.flatMap(_.argument), Seq(PathResponse(200, "")), Seq()))
  }

  def apply(): Renderer =
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
