package io.fintrospect.util.json

import io.fintrospect.ContentTypes
import io.fintrospect.util.{ResponseBuilder, ResponseBuilderObject}

class JsonResponseBuilder[T, N](format: JsonFormat[T, N]) extends ResponseBuilderObject[T] {
  private def formatJson(node: T): String = format.pretty(node)

  private def formatErrorMessage(errorMessage: String): T = format.obj("message" -> format.string(errorMessage))

  private def formatError(throwable: Throwable): T = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

  override def Response() = new ResponseBuilder[T](formatJson, formatErrorMessage, formatError, ContentTypes.APPLICATION_JSON)
}
