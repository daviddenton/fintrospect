package io.fintrospect

import io.fintrospect.util.XmlResponseBuilder

class XmlResponseBuilderTest extends ResponseBuilderObjectSpec(XmlResponseBuilder) {
  override val expectedContent = message
  override val expectedErrorContent = s"<message>$message</message>"
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialised: String = customType.toString()
}
