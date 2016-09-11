package examples.customformats

import com.twitter.io.Buf
import io.fintrospect.ContentTypes
import io.fintrospect.formats.{AbstractResponseBuilder, ResponseBuilder}

object HipsterXml {

  /**
    * Custom response builder for some imaginary XML format.
    */
  object ResponseBuilder extends AbstractResponseBuilder[HipsterXmlFormat] {
    private def customToString(format: HipsterXmlFormat): String = format.asXmlMessage

    private def errorMessageToString(errorMessage: String): HipsterXmlFormat = HipsterXmlFormat(s"<message>oh noes!, an error: $errorMessage</message>")

    private def errorToString(throwable: Throwable): HipsterXmlFormat = HipsterXmlFormat(s"<error>oh noes!, an error: ${throwable.getMessage}</error>")

    override def HttpResponse() = new ResponseBuilder[HipsterXmlFormat](i => Buf.Utf8(customToString(i)), errorMessageToString, errorToString, ContentTypes.APPLICATION_XML)
  }

}
