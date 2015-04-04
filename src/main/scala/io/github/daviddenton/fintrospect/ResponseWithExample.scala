package io.github.daviddenton.fintrospect

import argo.jdom.JsonNode
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpResponseStatus

case class ResponseWithExample(status: HttpResponseStatus, description: String, example: JsonNode = null)

object ResponseWithExample {
  val ERROR_EXAMPLE = obj("message" -> string("an error message goes here"))
}