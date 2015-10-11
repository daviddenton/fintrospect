package io.fintrospect.formats.text

import io.fintrospect.ContentTypes
import io.fintrospect.formats.{ResponseBuilder, ResponseBuilderMethods}

import scala.xml.Elem

object XHtmlResponseBuilder extends ResponseBuilderMethods[Elem] {

  private def format(node: Elem): String = node.toString()

  private def formatErrorMessage(errorMessage: String): Elem = <message>{errorMessage}</message>

  private def formatError(throwable: Throwable): Elem = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

  override def HttpResponse() = new ResponseBuilder[Elem](format, formatErrorMessage, formatError, ContentTypes.TEXT_HTML)
}