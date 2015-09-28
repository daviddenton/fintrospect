package io.fintrospect

import io.fintrospect.formats.{ResponseBuilderMethodsSpec, XmlResponseBuilder}

class XmlResponseBuilderTest extends ResponseBuilderMethodsSpec(XmlResponseBuilder) {
  override val expectedContent = message
  override val expectedErrorContent = s"<message>$message</message>"
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialised: String = customType.toString()
}
