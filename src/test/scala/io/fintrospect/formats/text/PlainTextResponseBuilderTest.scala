package io.fintrospect.formats.text

import io.fintrospect.formats.ResponseBuilderMethodsSpec

class PlainTextResponseBuilderTest extends ResponseBuilderMethodsSpec(PlainTextResponseBuilder) {
  override val expectedContent = message
  override val customError = PlainText(message)
  override val expectedErrorContent = message
  override val customType = PlainText(message)
  override val customTypeSerialised = customType.value

}
