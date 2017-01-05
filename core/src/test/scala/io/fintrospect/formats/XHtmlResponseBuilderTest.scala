package io.fintrospect.formats

import com.twitter.io.Bufs

class XHtmlResponseBuilderTest extends ResponseBuilderSpec(XHtml.ResponseBuilder) {
  override val customError = <message>{message}</message>
  override val customErrorSerialized = Bufs.utf8Buf(s"<message>$message</message>")
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialized = Bufs.utf8Buf(customType.toString())
}
