package io.fintrospect.renderers

import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status.{BadRequest, NotFound}
import io.fintrospect.formats.json.Argo.JsonFormat.{array, obj, string}
import io.fintrospect.formats.json.Argo.ResponseBuilder.implicits.{responseBuilderToResponse, statusToResponseBuilderConfig}
import io.fintrospect.util.ExtractionError

object JsonErrorResponseRenderer {
  def badRequest(badParameters: Seq[ExtractionError]): Response = {
    val messages = badParameters.map(p => obj(
      "name" -> string(p.name),
      "reason" -> string(p.reason)
    ))

    BadRequest(obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages)))
  }

  def notFound(): Response = {
    NotFound(obj("message" -> string("No route found on this path. Have you used the correct HTTP verb?")))
  }
}
