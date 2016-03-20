package io.fintrospect.renderers

import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status.{BadRequest, NotFound}
import io.fintrospect.formats.json.Argo.JsonFormat.{array, boolean, obj, string}
import io.fintrospect.formats.json.Argo.ResponseBuilder.implicits.{responseBuilderToResponse, statusToResponseBuilderConfig}
import io.fintrospect.parameters.Parameter

object JsonErrorResponseRenderer {
  def badRequest(badParameters: Seq[Parameter]): Response = {
    val messages = badParameters.map(p => obj(
      "name" -> string(p.name),
      "type" -> string(p.where),
      "datatype" -> string(p.paramType.name),
      "required" -> boolean(p.required)
    ))

    BadRequest(obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages)))
  }

  def notFound(): Response = { NotFound(obj("message" -> string("No route found on this path. Have you used the correct HTTP verb?"))) }
}
