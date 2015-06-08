package io.fintrospect.renderers.swagger2dot0

import argo.jdom.JsonNode
import com.twitter.finagle.http.path.Path
import io.fintrospect._
import io.fintrospect.parameters.{Body, Parameter, RequestParameter}
import io.fintrospect.renderers.util.{JsonToJsonSchema, Schema}
import io.fintrospect.renderers.{JsonBadRequestRenderer, ModuleRenderer}
import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.JsonResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponse

/**
 * ModuleRenderer that provides fairly comprehensive Swagger v2.0 support
 */
case class Swagger2dot0Json(apiInfo: ApiInfo) extends ModuleRenderer {

  def badRequest(badParameters: List[RequestParameter[_, _]]): HttpResponse = JsonBadRequestRenderer(badParameters)

  private val schemaGenerator = new JsonToJsonSchema()

  private case class FieldAndDefinitions(field: Field, definitions: List[Field])

  private case class FieldsAndDefinitions(fields: List[Field] = Nil, definitions: List[Field] = Nil) {
    def add(newField: Field, newDefinitions: List[Field]) = FieldsAndDefinitions(newField :: fields, newDefinitions ++ definitions)

    def add(fieldAndDefinitions: FieldAndDefinitions) = FieldsAndDefinitions(fieldAndDefinitions.field :: fields, fieldAndDefinitions.definitions ++ definitions)
  }

  private def render(parameter: Parameter[_]): JsonNode = obj(
    "in" -> string(parameter.location.toString),
    "name" -> string(parameter.name),
    "description" -> parameter.description.map(string).getOrElse(nullNode()),
    "required" -> boolean(parameter.required),
    "type" -> string(parameter.paramType.name)
  )

  private def render(body: Body, schema: Schema): JsonNode = obj(
    "in" -> string(body.location.toString),
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
      "supportedContentTypes" -> array(route.describedRoute.produces.map(m => string(m.value))),
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
