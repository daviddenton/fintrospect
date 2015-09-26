package io.fintrospect

import io.fintrospect.util.json.ArgoJsonFormat._
import io.fintrospect.util.json.{ArgoJsonFormat, ArgoJsonResponseBuilder}

class ArgoJsonResponseBuilderTest extends ResponseBuilderObjectSpec(ArgoJsonResponseBuilder) {
  override val expectedContent = message
  override val expectedErrorContent = pretty(obj("message" -> string(message)))
  override val customType = obj("okThing" -> string("theMessage"))
  override val customTypeSerialised: String = pretty(customType)
}
