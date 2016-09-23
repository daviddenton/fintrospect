package io.fintrospect.renderers.swagger2dot0

import argo.jdom.JsonNode
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.Argo.JsonFormat.{Field, array, boolean, nullNode, obj, parse, string}
import io.fintrospect.formats.Argo.ResponseBuilder.implicits.{responseBuilderToResponse, statusToResponseBuilderConfig}
import io.fintrospect.parameters.Parameter
import io.fintrospect.renderers.util.{JsonToJsonSchema, Schema}
import io.fintrospect.renderers.{JsonErrorResponseRenderer, ModuleRenderer}
import io.fintrospect.util.ExtractionError
import io.fintrospect.{ApiKey, NoSecurity, ResponseSpec, Security, ServerRoute}

import scala.util.Try

/**
  * ModuleRenderer that provides fairly comprehensive Swagger v2.0 support
  */
case class Swagger2dot0Json(apiInfo: ApiInfo) extends ModuleRenderer {

  override def badRequest(badParameters: Seq[ExtractionError]): Response = JsonErrorResponseRenderer.badRequest(badParameters)

  override def notFound(request: Request): Response = JsonErrorResponseRenderer.notFound()

  private val schemaGenerator = new JsonToJsonSchema()

  private case class FieldAndDefinitions(field: Field, definitions: Seq[Field])

  private case class FieldsAndDefinitions(fields: Seq[Field] = Nil, definitions: Seq[Field] = Nil) {
    def add(newField: Field, newDefinitions: Seq[Field]) = FieldsAndDefinitions(newField +: fields, newDefinitions ++ definitions)

    def add(fieldAndDefinitions: FieldAndDefinitions) = FieldsAndDefinitions(fieldAndDefinitions.field +: fields, fieldAndDefinitions.definitions ++ definitions)
  }

  private def render(parameter: Parameter, schema: Option[Schema]): JsonNode =
    obj(
      "in" -> string(parameter.where),
      "name" -> string(parameter.name),
      "description" -> parameter.description.map(string).getOrElse(nullNode()),
      "required" -> boolean(parameter.required),
      schema.map("schema" -> _.node).getOrElse("type" -> string(parameter.paramType.name))
    )

  private def render(basePath: Path, security: Security, route: ServerRoute[_, _]): FieldAndDefinitions = {
    val FieldsAndDefinitions(responses, responseDefinitions) = render(route.routeSpec.responses)

    val bodyParameters = route.routeSpec.body.flatMap(p => Option(p.toList)).getOrElse(Nil)

    val bodyAndSchemaAndRendered = bodyParameters.map(p => {
      val exampleOption = p.example.flatMap(s => Try(parse(s)).toOption).map(schemaGenerator.toSchema)
      (p, exampleOption, render(p, exampleOption))
    })

    val allParams = route.pathParams.flatten ++ route.routeSpec.requestParams
    val nonBodyParams = allParams.map(render(_, Option.empty))

    val jsonRoute = route.method.toString().toLowerCase -> obj(
      "tags" -> array(string(basePath.toString)),
      "summary" -> string(route.routeSpec.summary),
      "description" -> route.routeSpec.description.map(string).getOrElse(nullNode()),
      "produces" -> array(route.routeSpec.produces.map(m => string(m.value))),
      "consumes" -> array(route.routeSpec.consumes.map(m => string(m.value))),
      "parameters" -> array(nonBodyParams ++ bodyAndSchemaAndRendered.map(_._3)),
      "responses" -> obj(responses),
      "supportedContentTypes" -> array(route.routeSpec.produces.map(m => string(m.value))),
      "security" -> array(security match {
        case NoSecurity => Nil
        case ApiKey(param, _) => Seq(obj("api_key" -> array()))
      })
    )
    FieldAndDefinitions(jsonRoute, responseDefinitions ++ bodyAndSchemaAndRendered.flatMap(_._2).flatMap(_.definitions))
  }

  private def render(responses: Seq[ResponseSpec]): FieldsAndDefinitions =
    responses.foldLeft(FieldsAndDefinitions()) {
      case (memo, nextResp) =>
        val newSchema = Try(parse(nextResp.example.get)).toOption.map(schemaGenerator.toSchema).getOrElse(Schema(nullNode(), Nil))
        val newField = nextResp.status.code.toString -> obj("description" -> string(nextResp.description), "schema" -> newSchema.node)
        memo.add(newField, newSchema.definitions)
    }

  private def render(security: Security) = security match {
    case NoSecurity => obj()
    case ApiKey(param, _) => obj(Seq(
      "api_key" -> obj(
        "type" -> string("apiKey"),
        "in" -> string(param.where),
        "name" -> string(param.name)
      )
    ))
  }

  private def render(apiInfo: ApiInfo): JsonNode =
    obj("title" -> string(apiInfo.title), "version" -> string(apiInfo.version), "description" -> string(apiInfo.description.getOrElse("")))

  override def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_, _]]): Response = {
    val pathsAndDefinitions = routes
      .groupBy(_.describeFor(basePath))
      .foldLeft(FieldsAndDefinitions()) {
        case (memo, (path, routesForThisPath)) =>
          val routeFieldsAndDefinitions = routesForThisPath.foldLeft(FieldsAndDefinitions()) {
            case (memoFields, route) => memoFields.add(render(basePath, security, route))
          }
          memo.add(path -> obj(routeFieldsAndDefinitions.fields), routeFieldsAndDefinitions.definitions)
      }

    Ok(obj(
      "swagger" -> string("2.0"),
      "info" -> render(apiInfo),
      "basePath" -> string("/"),
      "paths" -> obj(pathsAndDefinitions.fields),
      "securityDefinitions" -> render(security),
      "definitions" -> obj(pathsAndDefinitions.definitions)
    ))
  }
}
