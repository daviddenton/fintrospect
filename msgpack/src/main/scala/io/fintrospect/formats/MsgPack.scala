package io.fintrospect.formats

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray.Shared.extract
import io.fintrospect.ContentTypes.APPLICATION_X_MSGPACK
import io.fintrospect.formats.MsgPack.Format.{decode, encode}
import io.fintrospect.parameters.{Body, BodySpec}

/**
  * MsgPack support (application/x-msgpack content type)
  */
object MsgPack {

  /**
    * Auto-marshalling filters that can be used to create Services which take and return domain objects
    * instead of HTTP responses
    */
  object Filters2 extends NuAutoFilters[MsgPackMsg](ResponseBuilder) {
    implicit def tToToOut[T]: AsOut[T, MsgPackMsg] = MsgPackMsg(_)
  }

  /**
    * Auto-marshalling filters which can be used to create Services which take and return MsgPackMsg objects
    * instead of HTTP responses
    */
  object Filters extends AutoFilters[MsgPackMsg] {

    override protected val responseBuilder = MsgPack.ResponseBuilder

    import MsgPack.ResponseBuilder._

    private def toResponse[OUT](successStatus: Status) = (t: OUT) => HttpResponse(successStatus).withContent(MsgPackMsg(t))

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOut[BODY, OUT](svc: Service[BODY, OUT], successStatus: Status = Status.Ok)
                            (implicit example: BODY = null, mf: Manifest[BODY])
    : Service[Request, Response] = AutoInOutFilter(successStatus)(example, mf).andThen(svc)

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which may return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoInOptionalOut[BODY, OUT](svc: Service[BODY, Option[OUT]], successStatus: Status = Status.Ok)
                                    (implicit example: BODY = null, mf: Manifest[BODY])
    : Service[Request, Response] = _AutoInOptionalOut(svc, Body(bodySpec[BODY](None)(mf), example), toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of output case class instances for HTTP scenarios where an object is returned.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoOut[IN, OUT](successStatus: Status = Status.Ok): Filter[IN, Response, IN, OUT] = _AutoOut(toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP scenarios where an object may not be returned
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoOptionalOut[IN, OUT](successStatus: Status = Status.Ok): Filter[IN, Response, IN, Option[OUT]] =
      _AutoOptionalOut(toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP POST scenarios
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOutFilter[BODY, OUT](successStatus: Status = Status.Ok)(implicit example: BODY = null, mf: Manifest[BODY])
    : Filter[Request, Response, BODY, OUT] = AutoIn(Body(bodySpec[BODY](None)(mf), example)).andThen(AutoOut[BODY, OUT](successStatus))
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