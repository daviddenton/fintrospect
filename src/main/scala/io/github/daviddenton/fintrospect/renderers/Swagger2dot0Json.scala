package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.FintrospectModule.Renderer
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Parameter
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.HttpResponseStatus.OK

object Swagger2dot0Json {

  private case class PathMethod(method: HttpMethod, summary: String, params: Seq[Parameter[_]], responses: Seq[PathResponse], securities: Seq[Security])

  private def render(p: Parameter[_]): JsonNode = obj(
    "in" -> string(p.where.toString),
    "name" -> string(p.name),
    "required" -> booleanNode(p.required.required),
    "type" -> string(p.paramType)
  )

  private def render(r: ModuleRoute): (String, JsonNode) = {
    r.description.method.getName.toLowerCase -> obj(
      "summary" -> string(r.description.value),
      "produces" -> array(string("application/json")),
      "parameters" -> array(r.params.map(render): _*),
      "responses" -> obj(Seq(PathResponse(OK, "")).map(r => r.code.getCode.toString -> obj("description" -> string(r.description)))),
      "security" -> array(obj(Seq[Security]().map(_.toPathSecurity)))
    )
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
