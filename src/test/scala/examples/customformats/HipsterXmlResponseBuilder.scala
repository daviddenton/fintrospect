package examples.customformats

import io.fintrospect.ContentTypes
import io.fintrospect.util.{ResponseBuilder, ResponseBuilderMethods}

/**
 * Custom response builder for some imaginary XML format.
 */
object HipsterXmlResponseBuilder extends ResponseBuilderMethods[HipsterXmlFormat] {

  private def customToString(format: HipsterXmlFormat): String = format.asXmlMessage

  private def errorMessageToString(errorMessage: String): HipsterXmlFormat = HipsterXmlFormat(s"<message>oh noes!, an error: $errorMessage</message>")

  private def errorToString(throwable: Throwable): HipsterXmlFormat = HipsterXmlFormat(s"<error>oh noes!, an error: ${throwable.getMessage}</error>")

  override def Response() = new ResponseBuilder[HipsterXmlFormat](customToString, errorMessageToString, errorToString, ContentTypes.APPLICATION_XML)
}
