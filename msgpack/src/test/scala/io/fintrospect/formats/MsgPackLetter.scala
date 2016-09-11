package io.fintrospect.formats

case class MsgPackLetter(to: MsgPackStreetAddress, from: MsgPackStreetAddress, message: String)
