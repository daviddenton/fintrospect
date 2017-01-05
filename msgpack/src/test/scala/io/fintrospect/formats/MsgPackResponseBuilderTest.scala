package io.fintrospect.formats

class MsgPackResponseBuilderTest extends ResponseBuilderSpec(MsgPack.ResponseBuilder) {
  override val customError = MsgPackMsg(message)
  override val customErrorSerialized = MsgPackMsg(message).toBuf
  override val customType = MsgPackMsg(message)
  override val customTypeSerialized = customType.toBuf
}
