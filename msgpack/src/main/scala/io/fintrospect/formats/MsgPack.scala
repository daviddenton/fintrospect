package io.fintrospect.formats

import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray.Shared.extract
import io.fintrospect.ContentTypes.APPLICATION_X_MSGPACK
import io.fintrospect.formats.MsgPack.Format.{decode, encode}
import io.fintrospect.parameters.{Body, BodySpec, UniBody}

/**
  * MsgPack support (application/x-msgpack content type)
  */
object MsgPack {

  /**
    * Auto-marshalling Service wrappers that can be used to create Services which take and return domain objects
    * instead of HTTP responses
    */
  object Auto extends Auto[MsgPackMsg](ResponseBuilder) {
    implicit def tToBody[T](implicit mf: Manifest[T]): UniBody[T] = Body(bodySpec[T]())

    implicit def tToMsgPackMsg[T]: Transform[T, MsgPackMsg] = MsgPackMsg(_)
  }

  /**
    * Convenience format handling methods
    */
  object Format {
    def decode[T](buf: Buf)(implicit mf: Manifest[T]): T = new MsgPackMsg(extract(buf)).as[T](mf)

    def encode[T](in: T): Buf = MsgPackMsg(in).toBuf
  }

  /**
    * Convenience body spec method
    */
  def bodySpec[T](description: Option[String] = None)(implicit mf: Manifest[T]): BodySpec[T] =
    BodySpec.binary(description, APPLICATION_X_MSGPACK).map(buf => decode(buf), m => encode(m))

  object ResponseBuilder extends AbstractResponseBuilder[MsgPackMsg] {

    private case class Error(message: String)

    private def formatErrorMessage(errorMessage: String): MsgPackMsg = MsgPackMsg(Error(errorMessage))

    private def formatError(throwable: Throwable): MsgPackMsg = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[MsgPackMsg](_.toBuf, formatErrorMessage, formatError, APPLICATION_X_MSGPACK)
  }

}