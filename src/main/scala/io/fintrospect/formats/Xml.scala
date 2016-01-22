package io.fintrospect.formats

import io.fintrospect.ContentTypes

import scala.xml.Elem

/**
  * Native (Elem-based) Xml support (application/xml content type)
  */
object Xml {
  object ResponseBuilder extends AbstractResponseBuilder[Elem] {

    private def format(node: Elem): String = node.toString()

    private def formatErrorMessage(errorMessage: String): Elem = <message>{errorMessage}</message>

    private def formatError(throwable: Throwable): Elem = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[Elem](format, formatErrorMessage, formatError, ContentTypes.APPLICATION_XML)
  }
}