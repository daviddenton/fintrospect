package io.fintrospect.util.json

import io.fintrospect.ResponseBuilderObjectSpec

abstract class JsonResponseBuilderSpec[X, Y](jsonLibrary: JsonLibrary[X, Y]) extends ResponseBuilderObjectSpec[X](jsonLibrary.ResponseBuilder) {
  override val expectedContent = message
  override val expectedErrorContent = jsonLibrary.JsonFormat.pretty(jsonLibrary.JsonFormat.obj("message" -> jsonLibrary.JsonFormat.string(message)))
  override val customType = jsonLibrary.JsonFormat.obj("okThing" -> jsonLibrary.JsonFormat.string("theMessage"))
  override val customTypeSerialised: String = jsonLibrary.JsonFormat.pretty(customType)
}
