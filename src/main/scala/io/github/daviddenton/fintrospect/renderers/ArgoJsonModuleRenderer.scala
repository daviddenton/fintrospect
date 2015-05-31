package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Route
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.JsonResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

import scala.language.implicitConversions

/**
 * ModuleRenderer providing Argo JSON format
 * @param descriptionRenderer converts the module routes into JSON format which can be rendered
 */
class ArgoJsonModuleRenderer(descriptionRenderer: DescriptionRenderer) extends ModuleRenderer {

  override def badRequest(badParameters: List[RequestParameter[_]]): HttpResponse = {
    val messages = badParameters.map(p => obj(
      "name" -> string(p.name),
      "type" -> string(p.where),
      "datatype" -> string(p.paramType.name),
      "required" -> boolean(p.requirement.required)
    ))

    Error(BAD_REQUEST, obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages)))
  }

  override def description(basePath: Path, routes: Seq[Route]): HttpResponse = descriptionRenderer.apply(basePath, routes)
}

