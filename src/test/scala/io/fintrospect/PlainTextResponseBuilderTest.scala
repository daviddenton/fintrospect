package io.fintrospect

import io.fintrospect.util.{PlainText, PlainTextResponseBuilder}

class PlainTextResponseBuilderTest extends ResponseBuilderMethodsSpec(PlainTextResponseBuilder) {
  override val expectedContent = message
  override val expectedErrorContent = message
  override val customType = PlainText(message)
  override val customTypeSerialised = customType.value

}
