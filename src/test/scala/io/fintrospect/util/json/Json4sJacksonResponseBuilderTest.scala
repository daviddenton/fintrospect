package io.fintrospect.util.json

import io.fintrospect.ResponseBuilderObjectSpec

class Json4sJacksonResponseBuilderTest extends ResponseBuilderObjectSpec(Json4s.jackson().ResponseBuilder) {
  private val format = Json4s.jackson().JsonFormat

  override val expectedContent = message
  override val expectedErrorContent = format.pretty(format.obj("message" -> format.string(message)))
  override val customType = format.obj("okThing" -> format.string("theMessage"))
  override val customTypeSerialised: String = format.pretty(customType)
}
