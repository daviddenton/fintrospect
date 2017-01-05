package io.fintrospect.formats

import com.twitter.io.Buf
import io.fintrospect.ContentTypes

import scala.xml.Elem

/**
  * Native (Elem-based) Xml support (application/xml content type)
  */
object Xml {

  /**
    * Auto-marshalling service wrappers which can be used to create Services which take and return Elem objects
    * instead of HTTP responses
    */
  object Auto extends Auto[Elem](ResponseBuilder)

  object ResponseBuilder extends AbstractResponseBuilder[Elem] {

    private def format(node: Elem): String = node.toString()

    private def formatErrorMessage(errorMessage: String): Elem = <message>
      {errorMessage}
    </message>

    private def formatError(throwable: Throwable): Elem = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[Elem](i => Buf.Utf8(format(i)), formatErrorMessage, formatError, ContentTypes.APPLICATION_XML)
  }

}