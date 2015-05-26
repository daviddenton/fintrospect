package io.github.daviddenton.fintrospect.util

import argo.format.PrettyJsonFormatter
import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.ContentTypes
import io.github.daviddenton.fintrospect.util.ArgoUtil._

object JsonResponseBuilder extends ResponseBuilderObject[JsonRootNode] {

  private def formatJson(node: JsonRootNode): String = new PrettyJsonFormatter().format(node)

  private def formatErrorMessage(errorMessage: String): JsonRootNode = obj("message" -> string(errorMessage))

  private def formatError(throwable: Throwable): JsonRootNode = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

  override def Response() = new ResponseBuilder[JsonRootNode](formatJson, formatErrorMessage, formatError, ContentTypes.APPLICATION_JSON)
}