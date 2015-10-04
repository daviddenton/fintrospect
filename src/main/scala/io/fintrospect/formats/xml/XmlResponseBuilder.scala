package io.fintrospect.formats.xml

import io.fintrospect.ContentTypes
import io.fintrospect.formats.{ResponseBuilder, ResponseBuilderMethods}

import scala.xml.Elem

object XmlResponseBuilder extends ResponseBuilderMethods[Elem] {

  private def formatJson(node: Elem): String = node.toString()

  private def formatErrorMessage(errorMessage: String): Elem = <message>{errorMessage}</message>

  private def formatError(throwable: Throwable): Elem = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

  override def HttpResponse() = new ResponseBuilder[Elem](formatJson, formatErrorMessage, formatError, ContentTypes.APPLICATION_XML)
}