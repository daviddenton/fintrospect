package io.fintrospect.util

import io.fintrospect.ContentTypes

import scala.xml.Elem

object XmlResponseBuilder extends ResponseBuilderObject[Elem] {

  private def formatJson(node: Elem): String = node.toString()

  private def formatErrorMessage(errorMessage: String): Elem = <message>{errorMessage}</message>

  private def formatError(throwable: Throwable): Elem = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

  override def Response() = new ResponseBuilder[Elem](formatJson, formatErrorMessage, formatError, ContentTypes.APPLICATION_XML)
}