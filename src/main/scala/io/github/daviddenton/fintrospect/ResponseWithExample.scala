package io.github.daviddenton.fintrospect

import argo.jdom.JsonNode
import org.jboss.netty.handler.codec.http.HttpResponseStatus

case class ResponseWithExample(status: HttpResponseStatus, description: String, example: JsonNode = null)
