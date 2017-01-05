package io.fintrospect.formats

import com.twitter.io.Bufs
import io.fintrospect.formats.Html.$

class HtmlResponseBuilderTest extends ResponseBuilderSpec(Html.ResponseBuilder) {
  override val customError = $(message)
  override val customErrorSerialized = Bufs.utf8Buf(message)
  override val customType = $("theMessage")
  override val customTypeSerialized = Bufs.utf8Buf(customType.value)
}
