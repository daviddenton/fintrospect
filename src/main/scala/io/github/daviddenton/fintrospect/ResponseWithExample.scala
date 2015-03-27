package io.github.daviddenton.fintrospect

import argo.jdom.JsonNode
import io.github.daviddenton.fintrospect.util.ArgoUtil.nullNode
import org.jboss.netty.handler.codec.http.HttpResponseStatus

case class ResponseWithExample(status: HttpResponseStatus, description: String, example: JsonNode = nullNode())
