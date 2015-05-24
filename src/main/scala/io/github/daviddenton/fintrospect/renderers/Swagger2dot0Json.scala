package io.github.daviddenton.fintrospect.renderers

import argo.jdom.{JsonNode, JsonRootNode}
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Body, Parameter, Requirement}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

class Swagger2dot0Json private(apiInfo: ApiInfo) extends DescriptionRenderer[JsonRootNode] {

  private val schemaGenerator = new JsonToJsonSchema()

  private case class FieldAndDefinitions(field: Field, definitions: List[Field])

  private case class FieldsAndDefinitions(fields: List[Field] = Nil, definitions: List[Field] = Nil) {
    def add(newField: Field, newDefinitions: List[Field]) = FieldsAndDefinitions(newField :: fields, newDefinitions ++ definitions)

    def add(fieldAndDefinitions: FieldAndDefinitions) = FieldsAndDefinitions(fieldAndDefinitions.field :: fields, fieldAndDefinitions.definitions ++ definitions)
  }

  private def render(requirementAndParameter: (Requirement, Parameter[_])): JsonNode = obj(
    "in" -> string(requirementAndParameter._2.where.toString),
    "name" -> string(requirementAndParameter._2.name),
    "description" -> requirementAndParameter._2.description.map(string).getOrElse(nullNode()),
    "required" -> boolean(requirementAndParameter._1.required),
    "type" -> string(requirementAndParameter._2.paramType.name)
  )

  private def render(body: Body, schema: Schema): JsonNode = obj(
    "in" -> string(body.where.toString),
    "name" -> string(body.name),
    "description" -> body.description.map(string).getOrElse(nullNode()),
    "required" -> boolean(true),
    "schema" -> schema.node
  )

  private def render(basePath: Path, route: Route): FieldAndDefinitions = {
    val FieldsAndDefinitions(responses, responseDefinitions) = render(route.describedRoute.responses)

    val bodySchema = route.describedRoute.body.map(b => schemaGenerator.toSchema(b.example))
    val bodyParameters = bodySchema.toList.flatMap(s => Seq(render(route.describedRoute.body.get, s)))

    val route2 = route.method.getName.toLowerCase -> obj(
      "tags" -> array(string(basePath.toString)),
      "summary" -> string(route.describedRoute.summary),
      "produces" -> array(route.describedRoute.produces.map(m => string(m.value))),
      "consumes" -> array(route.describedRoute.consumes.map(m => string(m.value))),
      "parameters" -> array(route.allParams.map(render) ++ bodyParameters),
      "responses" -> obj(responses),
      "security" -> array(obj(Seq[Security]().map(_.toPathSecurity)))
    )
    FieldAndDefinitions(route2, responseDefinitions ++ bodySchema.toList.flatMap(_.definitions))
  }

  private def render(responses: List[ResponseWithExample]): FieldsAndDefinitions = {
    responses.foldLeft(FieldsAndDefinitions()) {
      case (memo, nextResp) =>
        val newSchema = Option(nextResp.example).map(schemaGenerator.toSchema).getOrElse(Schema(nullNode(), Nil))
        val newField = nextResp.status.getCode.toString -> obj("description" -> string(nextResp.description), "schema" -> newSchema.node)
        memo.add(newField, newSchema.definitions)
    }
  }

  private def render(apiInfo: ApiInfo): JsonNode = {
    obj("title" -> string(apiInfo.title), "version" -> string(apiInfo.version), "description" -> string(apiInfo.description.getOrElse("")))
  }

  def apply(basePath: Path, routes: Seq[Route]): JsonRootNode = {
    val pathsAndDefinitions = routes
      .groupBy(_.describeFor(basePath))
      .foldLeft(FieldsAndDefinitions()) {
      case (memo, (path, routesForThisPath)) =>
        val routeFieldsAndDefinitions = routesForThisPath.foldLeft(FieldsAndDefinitions()) {
          case (memoFields, route) => memoFields.add(render(basePath, route))
        }
        memo.add(path -> obj(routeFieldsAndDefinitions.fields), routeFieldsAndDefinitions.definitions)
    }
    obj(
      "swagger" -> string("2.0"),
      "info" -> render(apiInfo),
      "basePath" -> string("/"),
      "paths" -> obj(pathsAndDefinitions.fields),
      "definitions" -> obj(pathsAndDefinitions.definitions)
    )
  }
}

/**
 * Renderer that provides Swagger v2.0 support
 */
object Swagger2dot0Json {
  def apply(apiInfo: ApiInfo) = new JsonModuleRenderer(new Swagger2dot0Json(apiInfo))
}

