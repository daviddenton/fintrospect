package io.fintrospect.formats.json

import io.fintrospect.ContentTypes
import io.fintrospect.formats.{AbstractResponseBuilder, ResponseBuilder}

/**
 * Defines a supported JSON library format (e.g. Argo or Json4s).
 * @tparam R - Root node type
 * @tparam N - Node type
 */
trait JsonLibrary[R, N] {

  /**
   * Use this to parse and create JSON objects in a generic way
   */
  val JsonFormat: JsonFormat[R, N]

  /**
   * Use this to create JSON-format Responses
   */
  object ResponseBuilder extends AbstractResponseBuilder[R] {
    private def formatJson(node: R): String = JsonFormat.compact(node)

    private def formatErrorMessage(errorMessage: String): R = JsonFormat.obj("message" -> JsonFormat.string(errorMessage))

    private def formatError(throwable: Throwable): R = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[R](formatJson, formatErrorMessage, formatError, ContentTypes.APPLICATION_JSON)
  }
}
