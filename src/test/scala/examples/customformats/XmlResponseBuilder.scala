package examples.customformats

import io.fintrospect.ContentTypes
import io.fintrospect.util.{ResponseBuilder, ResponseBuilderObject}

/**
 * Custom response builder for some imaginary XML format.
 */
object XmlResponseBuilder extends ResponseBuilderObject[XmlFormat] {

  private def customToString(format: XmlFormat): String = format.asXmlMessage

  private def errorMessageToString(errorMessage: String): XmlFormat = XmlFormat(s"<message>oh noes!, an error: $errorMessage</message>")

  private def errorToString(throwable: Throwable): XmlFormat = XmlFormat(s"<error>oh noes!, an error: ${throwable.getMessage}</error>")

  override def Response() = new ResponseBuilder[XmlFormat](customToString, errorMessageToString, errorToString, ContentTypes.APPLICATION_XML)
}
