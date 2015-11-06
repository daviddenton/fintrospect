package io.fintrospect.formats.xml

import io.fintrospect.formats.ResponseBuilderMethodsSpec

class XmlResponseBuilderTest extends ResponseBuilderMethodsSpec(XmlResponseBuilder) {
  override val expectedContent = message
  override val customError = <message>{message}</message>
  override val expectedErrorContent = s"<message>$message</message>"
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialised: String = customType.toString()
}
