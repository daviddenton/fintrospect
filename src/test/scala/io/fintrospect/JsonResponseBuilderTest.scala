package io.fintrospect

import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.JsonResponseBuilder

class JsonResponseBuilderTest extends ResponseBuilderObjectSpec(JsonResponseBuilder) {
  override val expectedContent = message
  override val expectedErrorContent = pretty(obj("message" -> string(message)))
  override val customType = obj("okThing" -> string("theMessage"))
  override val customTypeSerialised: String = pretty(customType)
}
