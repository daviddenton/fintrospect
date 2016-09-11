package io.fintrospect.formats

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf.ByteArray.Shared.extract
import io.fintrospect.ContentTypes
import io.fintrospect.ContentTypes.APPLICATION_X_MSGPACK
import io.fintrospect.parameters.{Body, BodySpec, StringParamType}

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

    private val body = Body(BodySpec(None, ContentTypes.APPLICATION_X_MSGPACK, s => new MsgPackMsg(extract(s)), (m: MsgPackMsg) => m.toBuf), null, StringParamType)

    private def toResponse(successStatus: Status = Ok) = (out: MsgPackMsg) => successStatus(out)

    /**
      * Wrap the enclosed service with auto-marshalling of input and output MsgPack instances for HTTP POST scenarios
      * which return an MsgPack.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOut(svc: Service[MsgPackMsg, MsgPackMsg], successStatus: Status = Ok): Service[Request, Response] =
    AutoIn(body).andThen(AutoOut[MsgPackMsg](successStatus)).andThen(svc)

    /**
      * Wrap the enclosed service with auto-marshalling of input and output MsgPackMsg instances for HTTP POST scenarios
      * which may return an MsgPackMsg.
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoInOptionalOut(svc: Service[MsgPackMsg, Option[MsgPackMsg]], successStatus: Status = Ok)
    : Service[Request, Response] = _AutoInOptionalOut[MsgPackMsg, MsgPackMsg](svc, body, toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of output MsgPackMsg instances for HTTP scenarios where an object is returned.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoOut[IN](successStatus: Status = Ok): Filter[IN, Response, IN, MsgPackMsg] = _AutoOut(toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of MsgPackMsg instances for HTTP scenarios where an object may not be returned
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoOptionalOut[IN](successStatus: Status = Ok): Filter[IN, Response, IN, Option[MsgPackMsg]]
    = _AutoOptionalOut(toResponse(successStatus))

  }

  object ResponseBuilder extends AbstractResponseBuilder[MsgPackMsg] {

    private case class Error(message: String)

    private def formatErrorMessage(errorMessage: String): MsgPackMsg = MsgPackMsg(Error(errorMessage))

    private def formatError(throwable: Throwable): MsgPackMsg = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[MsgPackMsg](_.toBuf, formatErrorMessage, formatError, APPLICATION_X_MSGPACK)
  }

}