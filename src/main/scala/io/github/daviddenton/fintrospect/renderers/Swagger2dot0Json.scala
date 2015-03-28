package io.github.daviddenton.fintrospect.renderers

import java.util.UUID

import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Parameter, Requirement}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

class Swagger2dot0Json private(apiInfo: ApiInfo) extends Renderer {

  private case class FieldAndDefinitions(field: Field, definitions: List[Field])

  private case class FieldsAndDefinitions(fields: List[Field] = Nil, definitions: List[Field] = Nil) {
    def add(newField: Field, newDefinitions: List[Field]) = FieldsAndDefinitions(newField :: fields, newDefinitions ++ definitions)
  }

  private val schemaGenerator = new JsonToJsonSchema(() => UUID.randomUUID().toString)

  private def render(requirementAndParameter: (Requirement, Parameter[_])): JsonNode = obj(
    "in" -> string(requirementAndParameter._2.where.toString),
    "name" -> string(requirementAndParameter._2.name),
    "description" -> requirementAndParameter._2.description.map(string).getOrElse(nullNode()),
    "required" -> boolean(requirementAndParameter._1.required),
    "type" -> string(requirementAndParameter._2.paramType)
  )

  private def renderRoute(r: ModuleRoute): FieldAndDefinitions = {
    val responsesAndDefinitions = renderResponses(r.description.responses)
    val route = r.on.method.getName.toLowerCase -> obj(
      "tags" -> array(string(r.basePath.toString)),
      "summary" -> r.description.summary.map(string).getOrElse(nullNode()),
      "produces" -> array(r.description.produces.map(m => string(m.value))),
      "consumes" -> array(r.description.consumes.map(m => string(m.value))),
      "parameters" -> array(r.allParams.map(render)),
      "responses" -> obj(responsesAndDefinitions.fields),
      "security" -> array(obj(Seq[Security]().map(_.toPathSecurity)))
    )
    FieldAndDefinitions(route, responsesAndDefinitions.definitions)
  }

  private def renderResponses(responses: List[ResponseWithExample]): FieldsAndDefinitions = {
    responses.foldLeft(FieldsAndDefinitions()) {
      case (memo, nextResp) =>
        val newSchema = Option(nextResp.example).map(schemaGenerator.toSchema).getOrElse(Schema(nullNode(), Nil))
        val newField = nextResp.status.getCode.toString -> obj("description" -> string(nextResp.description), "schema" -> newSchema.node)
        memo.add(newField, newSchema.modelDefinitions)
    }
  }

  private def renderApiInfo(apiInfo: ApiInfo): JsonNode = {
    obj("title" -> string(apiInfo.title), "version" -> string(apiInfo.version), "description" -> string(apiInfo.description.getOrElse("")))
  }

  def apply(moduleRoutes: Seq[ModuleRoute]): JsonRootNode = {
    val pathsAndDefinitions = moduleRoutes
      .groupBy(_.toString)
      .foldLeft(FieldsAndDefinitions()) {
      case (memo, (path, routesForThisPath)) =>
        val newField = path -> obj(routesForThisPath.map {
          case mr =>
            val routeAndDefinitions: FieldAndDefinitions = renderRoute(mr)
            routeAndDefinitions.field
        })
        memo.add(newField, Nil)
    }
    obj(
      "swagger" -> string("2.0"),
      "info" -> renderApiInfo(apiInfo),
      "basePath" -> string("/"),
      "paths" -> obj(pathsAndDefinitions.fields),
      "definitions" -> obj(pathsAndDefinitions.definitions)
    )
  }
}

object Swagger2dot0Json {
  def apply(apiInfo: ApiInfo): Renderer = new Swagger2dot0Json(apiInfo)
}