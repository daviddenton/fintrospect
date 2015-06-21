package io.fintrospect.renderers.swagger2dot0

import argo.jdom.JsonNode
import com.twitter.finagle.http.path.Path
import io.fintrospect._
import io.fintrospect.parameters.Parameter
import io.fintrospect.renderers.util.{JsonToJsonSchema, Schema}
import io.fintrospect.renderers.{JsonBadRequestRenderer, ModuleRenderer}
import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.JsonResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponse

/**
 * ModuleRenderer that provides fairly comprehensive Swagger v2.0 support
 */
case class Swagger2dot0Json(apiInfo: ApiInfo) extends ModuleRenderer {

  def badRequest(badParameters: Seq[Parameter[_]]): HttpResponse = JsonBadRequestRenderer(badParameters)

  private val schemaGenerator = new JsonToJsonSchema()

  private case class FieldAndDefinitions(field: Field, definitions: Seq[Field])

  private case class FieldsAndDefinitions(fields: Seq[Field] = Nil, definitions: Seq[Field] = Nil) {
    def add(newField: Field, newDefinitions: Seq[Field]) = FieldsAndDefinitions(newField +: fields, newDefinitions ++ definitions)

    def add(fieldAndDefinitions: FieldAndDefinitions) = FieldsAndDefinitions(fieldAndDefinitions.field +: fields, fieldAndDefinitions.definitions ++ definitions)
  }

  private def render(parameter: Parameter[_], schema: Option[Schema]): JsonNode = {
    val typeField = schema.map("schema" -> _.node).getOrElse("type" -> string(parameter.paramType.name))
    obj(
      "in" -> string(parameter.where),
      "name" -> string(parameter.name),
      "description" -> parameter.description.map(string).getOrElse(nullNode()),
      "required" -> boolean(parameter.required),
      typeField
    )
  }

  private def render(basePath: Path, route: Route): FieldAndDefinitions = {
    val FieldsAndDefinitions(responses, responseDefinitions) = render(route.describedRoute.responses)

    val bodyParameters = route.describedRoute.body.map(_.iterator).getOrElse(Nil)

    val bpAndSchemaAndRendered  = bodyParameters.map(p => (p, p.example.map(schemaGenerator.toSchema), render(p, p.example.map(schemaGenerator.toSchema))))

    val nonBodyParams = (route.describedRoute.requestParams ++ route.pathParams.flatMap(identity)).map(render(_, Option.empty))

    val route2 = route.method.getName.toLowerCase -> obj(
      "tags" -> array(string(basePath.toString)),
      "summary" -> string(route.describedRoute.summary),
      "produces" -> array(route.describedRoute.produces.map(m => string(m.value))),
      "consumes" -> array(route.describedRoute.consumes.map(m => string(m.value))),
      "parameters" -> array(nonBodyParams ++ bpAndSchemaAndRendered.map(_._3).toList),
      "responses" -> obj(responses),
      "supportedContentTypes" -> array(route.describedRoute.produces.map(m => string(m.value))),
      "security" -> array(obj(Seq[Security]().map(_.toPathSecurity)))
    )
    FieldAndDefinitions(route2, responseDefinitions ++ bpAndSchemaAndRendered.flatMap(_._2).flatMap(_.definitions))
  }

  private def render(responses: Seq[ResponseWithExample]): FieldsAndDefinitions = {
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

  override def description(basePath: Path, routes: Seq[Route]): HttpResponse = {
    val pathsAndDefinitions = routes
      .groupBy(_.describeFor(basePath))
      .foldLeft(FieldsAndDefinitions()) {

      case (memo, (path, routesForThisPath)) =>
        val routeFieldsAndDefinitions = routesForThisPath.foldLeft(FieldsAndDefinitions()) {
          case (memoFields, route) => memoFields.add(render(basePath, route))
        }
        memo.add(path -> obj(routeFieldsAndDefinitions.fields), routeFieldsAndDefinitions.definitions)
    }

    Ok(obj(
      "swagger" -> string("2.0"),
      "info" -> render(apiInfo),
      "basePath" -> string("/"),
      "paths" -> obj(pathsAndDefinitions.fields),
      "definitions" -> obj(pathsAndDefinitions.definitions)
    ))
  }
}
