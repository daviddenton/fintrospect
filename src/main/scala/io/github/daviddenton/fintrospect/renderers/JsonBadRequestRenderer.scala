package io.github.daviddenton.fintrospect.renderers
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.JsonResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

object JsonBadRequestRenderer {
  def apply(badParameters: List[RequestParameter[_]]): HttpResponse = {
    val messages = badParameters.map(p => obj(
      "name" -> string(p.name),
      "type" -> string(p.where),
      "datatype" -> string(p.paramType.name),
      "required" -> boolean(p.requirement.required)
    ))

    Error(BAD_REQUEST, obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages)))
  }
}
