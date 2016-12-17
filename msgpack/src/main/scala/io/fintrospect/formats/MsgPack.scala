package io.fintrospect.formats

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray.Shared.extract
import io.fintrospect.ContentTypes.APPLICATION_X_MSGPACK
import io.fintrospect.formats.MsgPack.Format.bodySpec
import io.fintrospect.parameters.{Body, BodySpec, FileParamType}

/**
  * MsgPack support (application/x-msgpack content type)
  */
object MsgPack {

  /**
    * Auto-marshalling filters which can be used to create Services which take and return MsgPackMsg objects
    * instead of HTTP responses
    */
  object Filters extends AutoFilters[MsgPackMsg] {

    override protected val responseBuilder = MsgPack.ResponseBuilder

    import MsgPack.ResponseBuilder.implicits._

    private def toResponse[OUT <: AnyRef](successStatus: Status) = (t: OUT) => successStatus(MsgPackMsg(t))

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOut[BODY <: AnyRef, OUT <: AnyRef](svc: Service[BODY, OUT], successStatus: Status = Ok)
                                                (implicit example: BODY = null, mf: scala.reflect.Manifest[BODY])
    : Service[Request, Response] = AutoInOutFilter(successStatus)(example, mf).andThen(svc)

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which may return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoInOptionalOut[BODY <: AnyRef, OUT <: AnyRef](svc: Service[BODY, Option[OUT]], successStatus: Status = Ok)
                                                        (implicit example: BODY = null, mf: scala.reflect.Manifest[BODY])
    : Service[Request, Response] = _AutoInOptionalOut(svc, Body(bodySpec[BODY](None)(mf), example), toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of output case class instances for HTTP scenarios where an object is returned.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoOut[IN, OUT <: AnyRef](successStatus: Status = Ok): Filter[IN, Response, IN, OUT] = _AutoOut(toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP scenarios where an object may not be returned
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoOptionalOut[IN, OUT <: AnyRef](successStatus: Status = Ok): Filter[IN, Response, IN, Option[OUT]] =
      _AutoOptionalOut(toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP POST scenarios
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOutFilter[BODY <: AnyRef, OUT <: AnyRef](successStatus: Status = Ok)(implicit example: BODY = null, mf: scala.reflect.Manifest[BODY])
    : Filter[Request, Response, BODY, OUT] = AutoIn(Body(bodySpec[BODY](None)(mf), example)).andThen(AutoOut[BODY, OUT](successStatus))
  }

  /**
    * Convenience format handling methods
    */
  object Format {
    def decode[T <: AnyRef](buf: Buf)(implicit mf: scala.reflect.Manifest[T]): T = new MsgPackMsg(extract(buf)).as[T](mf)

    def encode[T <: AnyRef](in: T): Buf = MsgPackMsg(in).toBuf

    def bodySpec[T <: AnyRef](description: Option[String] = None)(implicit mf: scala.reflect.Manifest[T]): BodySpec[T] =
      BodySpec(description, APPLICATION_X_MSGPACK, FileParamType,
        buf => new MsgPackMsg(extract(buf)), (m: MsgPackMsg) => m.toBuf)
        .map[T]((m: MsgPackMsg) => m.as[T](mf), (t: T) => MsgPackMsg(t))
  }

  object ResponseBuilder extends AbstractResponseBuilder[MsgPackMsg] {

    private case class Error(message: String)

    private def formatErrorMessage(errorMessage: String): MsgPackMsg = MsgPackMsg(Error(errorMessage))

    private def formatError(throwable: Throwable): MsgPackMsg = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[MsgPackMsg](_.toBuf, formatErrorMessage, formatError, APPLICATION_X_MSGPACK)
  }

}