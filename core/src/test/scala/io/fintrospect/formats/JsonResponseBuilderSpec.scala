package io.fintrospect.formats

abstract class JsonResponseBuilderSpec[X, Y](jsonLibrary: JsonLibrary[X, Y]) extends AbstractResponseBuilderSpec[X](jsonLibrary.ResponseBuilder) {
  override val expectedContent = message
  override val customError = jsonLibrary.JsonFormat.obj("message" -> jsonLibrary.JsonFormat.string(message))
  override val expectedErrorContent = jsonLibrary.JsonFormat.compact(jsonLibrary.JsonFormat.obj("message" -> jsonLibrary.JsonFormat.string(message)))
  override val customType = jsonLibrary.JsonFormat.obj("okThing" -> jsonLibrary.JsonFormat.string("theMessage"))
  override val customTypeSerialised: String = jsonLibrary.JsonFormat.compact(customType)
}
