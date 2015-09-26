package io.fintrospect.util.json

import argo.jdom.JsonRootNode
import io.fintrospect.ContentTypes
import io.fintrospect.util.json.ArgoJsonFormat._
import io.fintrospect.util.{ResponseBuilder, ResponseBuilderObject}

object ArgoJsonResponseBuilder extends ResponseBuilderObject[JsonRootNode] {

  private def formatJson(node: JsonRootNode): String = ArgoJsonFormat.pretty(node)

  private def formatErrorMessage(errorMessage: String): JsonRootNode = ArgoJsonFormat.obj("message" -> string(errorMessage))

  private def formatError(throwable: Throwable): JsonRootNode = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

  override def Response() = new ResponseBuilder[JsonRootNode](formatJson, formatErrorMessage, formatError, ContentTypes.APPLICATION_JSON)
}