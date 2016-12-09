package io.fintrospect.formats

import io.fintrospect.formats.Html.$

class HtmlResponseBuilderTest extends AbstractResponseBuilderSpec(Html.ResponseBuilder) {
  override val expectedContent = message
  override val customError = $(message)
  override val expectedErrorContent = message
  override val customType = $("theMessage")
  override val customTypeSerialized: String = customType.value
}
