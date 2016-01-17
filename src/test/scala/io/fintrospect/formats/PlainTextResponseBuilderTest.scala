package io.fintrospect.formats

class PlainTextResponseBuilderTest extends ResponseBuilderMethodsSpec(PlainText.ResponseBuilder) {
  override val expectedContent = message
  override val customError = PlainTextValue(message)
  override val expectedErrorContent = message
  override val customType = PlainTextValue(message)
  override val customTypeSerialised = customType.value

}
