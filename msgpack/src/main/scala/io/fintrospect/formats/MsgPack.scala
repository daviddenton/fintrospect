package io.fintrospect.formats

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf.ByteArray.Shared.extract
import io.fintrospect.ContentTypes
import io.fintrospect.ContentTypes.APPLICATION_X_MSGPACK
import io.fintrospect.parameters.{Body, BodySpec, ObjectParamType}

/**
  * MsgPack support (application/x-msgpack content type)
  */
object MsgPack {

  /**
    * Auto-marshalling filters which can be used to create Services which take and return MsgPackMsg objects
    * instead of HTTP responses
    */
  object Filters extends AutoFilters[MsgPackMsg](MsgPack.ResponseBuilder) {

    import MsgPack.ResponseBuilder.implicits._

    private def toResponse[OUT <: AnyRef](successStatus: Status) = (t: OUT) => successStatus(MsgPackMsg(t))

    private def toBody[BODY <: AnyRef](mf: scala.reflect.Manifest[BODY])(implicit example: BODY = null) = Body[BODY](bodySpec[BODY](None)(mf), example, ObjectParamType)

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
    : Service[Request, Response] = _AutoInOptionalOut(svc, toBody(mf), toResponse(successStatus))

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
    : Filter[Request, Response, BODY, OUT] = AutoIn(toBody(mf)).andThen(AutoOut[BODY, OUT](successStatus))
  }

  private def rawBodySpec(description: Option[String]) = BodySpec(description, ContentTypes.APPLICATION_X_MSGPACK, buf => new MsgPackMsg(extract(buf)), (m: MsgPackMsg) => m.toBuf)

  def bodySpec[T <: AnyRef](description: Option[String])(implicit mf: scala.reflect.Manifest[T]) =
    rawBodySpec(description).map[T]((m: MsgPackMsg) => m.as[T](mf), (t: T) => MsgPackMsg(t))

  /**
    * Convenience method for creating Body that just uses straight MsgPack encoding/decoding logic
    */
  def body[R <: AnyRef](description: Option[String] = None, example: R = null)
             (implicit mf: scala.reflect.Manifest[R]) = Body(bodySpec[R](description)(mf), example, ObjectParamType)

  object ResponseBuilder extends AbstractResponseBuilder[MsgPackMsg] {

    private case class Error(message: String)

    private def formatErrorMessage(errorMessage: String): MsgPackMsg = MsgPackMsg(Error(errorMessage))

    private def formatError(throwable: Throwable): MsgPackMsg = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[MsgPackMsg](_.toBuf, formatErrorMessage, formatError, APPLICATION_X_MSGPACK)
  }

}