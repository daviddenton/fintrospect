package io.fintrospect.util.json

import io.fintrospect.ResponseBuilderObjectSpec
import io.fintrospect.util.json.ArgoJsonFormat._

class ArgoJsonResponseBuilderTest extends ResponseBuilderObjectSpec(ArgoJsonResponseBuilder) {
  override val expectedContent = message
  override val expectedErrorContent = pretty(obj("message" -> string(message)))
  override val customType = obj("okThing" -> string("theMessage"))
  override val customTypeSerialised: String = pretty(customType)
}
