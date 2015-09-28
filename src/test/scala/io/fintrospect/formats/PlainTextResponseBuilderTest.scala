package io.fintrospect.formats

import io.fintrospect.formats.text.{PlainText, PlainTextResponseBuilder}

class PlainTextResponseBuilderTest extends ResponseBuilderMethodsSpec(PlainTextResponseBuilder) {
  override val expectedContent = message
  override val expectedErrorContent = message
  override val customType = PlainText(message)
  override val customTypeSerialised = customType.value

}
