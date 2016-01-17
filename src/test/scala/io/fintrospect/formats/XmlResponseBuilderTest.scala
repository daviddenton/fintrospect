package io.fintrospect.formats

class XmlResponseBuilderTest extends ResponseBuilderMethodsSpec(Xml.ResponseBuilder) {
  override val expectedContent = message
  override val customError = <message>{message}</message>
  override val expectedErrorContent = s"<message>$message</message>"
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialised: String = customType.toString()
}
