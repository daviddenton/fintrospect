package io.fintrospect.renderers

import com.twitter.finagle.httpx.{Response, Status}
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.formats.json.Argo.ResponseBuilder._
import io.fintrospect.parameters.Parameter

object JsonBadRequestRenderer {
  def apply(badParameters: Seq[Parameter]): Response = {
    val messages = badParameters.map(p => obj(
      "name" -> string(p.name),
      "type" -> string(p.where),
      "datatype" -> string(p.paramType.name),
      "required" -> boolean(p.required)
    ))

    Error(Status.BadRequest, obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages)))
  }
}
