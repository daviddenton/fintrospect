package io.fintrospect.renderers
import io.fintrospect.parameters.RequestParameter
import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.JsonResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

object JsonBadRequestRenderer {
  def apply(badParameters: List[RequestParameter[_]]): HttpResponse = {
    val messages = badParameters.map(p => obj(
      "name" -> string(p.name),
      "type" -> string(p.location.toString),
      "datatype" -> string(p.paramType.name),
      "required" -> boolean(p.required)
    ))

    Error(BAD_REQUEST, obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages)))
  }
}
