package io.fintrospect.formats

import com.twitter.io.Buf
import org.json4s.Extraction.decompose
import org.json4s.native.Serialization
import org.json4s.{JValue, NoTypeHints}
import org.velvia.msgpack.Json4sCodecs._
import org.velvia.msgpack._

class MsgPackMsg private[formats] (private val bytes: Array[Byte]) {
  def toBuf = Buf.ByteArray(bytes: _*)

  def as[OUT](implicit mf: scala.reflect.Manifest[OUT]): OUT = unpack[JValue](bytes).extract[OUT](MsgPackMsg.formats, mf)
}

object MsgPackMsg {

  private val formats = Serialization.formats(NoTypeHints)

  def apply[T <: AnyRef](in: T): MsgPackMsg = new MsgPackMsg(pack(decompose(in)(formats)))
}