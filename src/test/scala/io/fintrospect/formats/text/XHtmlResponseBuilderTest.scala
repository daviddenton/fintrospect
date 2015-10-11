package io.fintrospect.formats.text

import io.fintrospect.formats.ResponseBuilderMethodsSpec

class XHtmlResponseBuilderTest extends ResponseBuilderMethodsSpec(XHtmlResponseBuilder) {
  override val expectedContent = message
  override val expectedErrorContent = s"<message>$message</message>"
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialised: String = customType.toString()
}
