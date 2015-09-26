package io.fintrospect.renderers
import io.fintrospect.parameters.Parameter
import io.fintrospect.util.json.Argo.JsonFormat._
import io.fintrospect.util.json.Argo.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

object JsonBadRequestRenderer {
  def apply(badParameters: Seq[Parameter]): HttpResponse = {
    val messages = badParameters.map(p => obj(
      "name" -> string(p.name),
      "type" -> string(p.where),
      "datatype" -> string(p.paramType.name),
      "required" -> boolean(p.required)
    ))

    Error(BAD_REQUEST, obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages)))
  }
}
