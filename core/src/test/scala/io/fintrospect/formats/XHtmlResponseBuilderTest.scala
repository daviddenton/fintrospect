package io.fintrospect.formats

class XHtmlResponseBuilderTest extends ResponseBuilderSpec(XHtml.ResponseBuilder) {
  override val customError = <message>{message}</message>
  override val customErrorSerialized = s"<message>$message</message>"
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialized: String = customType.toString()
}
