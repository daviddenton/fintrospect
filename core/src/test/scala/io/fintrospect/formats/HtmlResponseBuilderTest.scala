package io.fintrospect.formats

import io.fintrospect.formats.Html.$

class HtmlResponseBuilderTest extends ResponseBuilderSpec(Html.ResponseBuilder) {
  override val customError = $(message)
  override val customErrorSerialized = message
  override val customType = $("theMessage")
  override val customTypeSerialized: String = customType.value
}
