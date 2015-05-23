package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.parameters.RequestParameter
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

trait ModuleResponseRenderer {
  def badRequest(badParameters: List[RequestParameter[_]]): HttpResponse
}

object ModuleResponseRenderer {
  val Json = new ModuleResponseRenderer {
    override def badRequest(badParameters: List[RequestParameter[_]]): HttpResponse = {
      val messages = badParameters.map(p => obj(
        "name" -> string(p.name),
        "type" -> string(p.where),
        "datatype" -> string(p.paramType.name),
        "required" -> boolean(p.requirement.required)
      ))
      ResponseBuilder.Json()
        .withCode(BAD_REQUEST)
        .withContent(obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages)))
        .build
    }
  }
}