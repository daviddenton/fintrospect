package io.fintrospect.formats

import com.twitter.io.Bufs

abstract class JsonResponseBuilderSpec[X <: Y, Y](jsonLibrary: JsonLibrary[X, Y]) extends ResponseBuilderSpec[X](jsonLibrary.ResponseBuilder) {
  override val customError = jsonLibrary.JsonFormat.obj("message" -> jsonLibrary.JsonFormat.string(message))
  override val customErrorSerialized = Bufs.utf8Buf(jsonLibrary.JsonFormat.compact(jsonLibrary.JsonFormat.obj("message" -> jsonLibrary.JsonFormat.string(message))))
  override val customType = jsonLibrary.JsonFormat.obj("okThing" -> jsonLibrary.JsonFormat.string("theMessage"))
  override val customTypeSerialized = Bufs.utf8Buf(jsonLibrary.JsonFormat.compact(customType))
}
