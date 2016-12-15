package io.fintrospect.formats

class XHtmlResponseBuilderTest extends AbstractResponseBuilderSpec(XHtml.ResponseBuilder) {
  override val expectedContent = message
  override val customError = <message>
    {message}
  </message>
  override val expectedErrorContent = s"<message>$message</message>"
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialized: String = customType.toString()
}
