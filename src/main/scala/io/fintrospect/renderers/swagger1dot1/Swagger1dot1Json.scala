package io.fintrospect.renderers.swagger1dot1

import argo.jdom.JsonNode
import com.twitter.finagle.http.path.Path
import io.fintrospect.Route
import io.fintrospect.parameters.{Parameter, ParseableParameter}
import io.fintrospect.renderers.{JsonBadRequestRenderer, ModuleRenderer}
import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.JsonResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponse

import scala.collection.JavaConversions._

/**
 * ModuleRenderer that provides basic Swagger v1.1 support. No support for bodies or schemas.
 */
class Swagger1dot1Json extends ModuleRenderer {

  private def render(parameter: Parameter[_]): JsonNode = obj(
    "name" -> string(parameter.name),
    "description" -> parameter.description.map(string).getOrElse(nullNode()),
    "paramType" -> string(parameter.where),
    "required" -> boolean(parameter.required),
    "dataType" -> string(parameter.paramType.name)
  )

  private def render(route: Route): Field = route.method.getName.toLowerCase -> {
    val bodyParameters = route.describedRoute.body.map(_.iterator).getOrElse(Nil)

    obj(
      "httpMethod" -> string(route.method.getName),
      "nickname" -> string(route.describedRoute.summary),
      "notes" -> string(route.describedRoute.summary),
      "produces" -> array(route.describedRoute.produces.map(m => string(m.value))),
      "consumes" -> array(route.describedRoute.consumes.map(m => string(m.value))),
      "parameters" -> array((route.describedRoute.requestParams ++ route.pathParams.flatMap(identity) ++ bodyParameters).map(render)),
      "errorResponses" -> array(route.describedRoute.responses
        .filter(_.status.getCode > 399)
        .map(resp => obj("code" -> number(resp.status.getCode), "reason" -> string(resp.description))).toSeq)
    )
  }

  override def description(basePath: Path, routes: Seq[Route]): HttpResponse = {
    val api = routes
      .groupBy(_.describeFor(basePath))
      .map { case (path, routesForPath) => obj("path" -> string(path), "operations" -> array(routesForPath.map(render(_)._2))) }

    Ok(obj("swaggerVersion" -> string("1.1"), "resourcePath" -> string("/"), "apis" -> array(asJavaIterable(api))))
  }

  override def badRequest(badParameters: Seq[ParseableParameter[_]]): HttpResponse = JsonBadRequestRenderer(badParameters)
}

object Swagger1dot1Json {
  def apply() = new Swagger1dot1Json()
}