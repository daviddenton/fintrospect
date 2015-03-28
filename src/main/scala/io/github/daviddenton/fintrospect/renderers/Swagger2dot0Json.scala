package io.github.daviddenton.fintrospect.renderers

import java.util.UUID

import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Parameter, Requirement}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

class Swagger2dot0Json private(apiInfo: ApiInfo) extends Renderer {

  private val schemaGenerator = new JsonToJsonSchema(() => UUID.randomUUID().toString)

  private def render(rp: (Requirement, Parameter[_])): JsonNode = obj(
    "in" -> string(rp._2.where.toString),
    "name" -> string(rp._2.name),
    "description" -> rp._2.description.map(string).getOrElse(nullNode()),
    "required" -> boolean(rp._1.required),
    "type" -> string(rp._2.paramType)
  )

  private def render(r: ModuleRoute): Field = {
    r.on.method.getName.toLowerCase -> obj(
      "tags" -> array(Seq(string(r.basePath.toString)): _*),
      "summary" -> r.description.summary.map(string).getOrElse(nullNode()),
      "produces" -> array(r.description.produces.map(m => string(m.value)): _*),
      "consumes" -> array(r.description.consumes.map(m => string(m.value)): _*),
      "parameters" -> array(r.allParams.map(render).toSeq: _*),
      "responses" -> obj(renderPaths(r)),
      "security" -> array(obj(Seq[Security]().map(_.toPathSecurity)))
    )
  }

  private def renderPaths(r: ModuleRoute) = {
    val (allFields, allModels) = r.description.responses.foldLeft((List[Field](), List[Field]())) {
      case ((fields, models), nextResp) =>
        val newSchema: Option[Schema] = Option(nextResp.example).map(schemaGenerator.toSchema)
        val schema = newSchema.getOrElse(Schema(nullNode(), Nil))
        val newField = nextResp.status.getCode.toString -> obj("description" -> string(nextResp.description), "schema" -> schema.node)
        (newField :: fields, schema.modelDefinitions ++ models)
    }
    allFields
  }

  private def render(apiInfo: ApiInfo): JsonRootNode = {
    obj("title" -> string(apiInfo.title), "version" -> string(apiInfo.version), "description" -> string(apiInfo.description.getOrElse("")))
  }

  def apply(mr: Seq[ModuleRoute]): JsonRootNode = {
    val paths = mr
      .groupBy(_.toString)
      .map { case (path, routes) => path -> obj(routes.map(render))}.toSeq

    obj(
      "swagger" -> string("2.0"),
      "info" -> render(apiInfo),
      "basePath" -> string("/"),
      "paths" -> obj(paths),
      "definitions" -> obj()
    )
  }
}


object Swagger2dot0Json {
  def apply(apiInfo: ApiInfo): Renderer = new Swagger2dot0Json(apiInfo)
}