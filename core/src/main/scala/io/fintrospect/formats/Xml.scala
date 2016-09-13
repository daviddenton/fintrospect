package io.fintrospect.formats

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf
import io.fintrospect.ContentTypes
import io.fintrospect.parameters.Body

import scala.xml.Elem

/**
  * Native (Elem-based) Xml support (application/xml content type)
  */
object Xml {

  /**
    * Auto-marshalling filters which can be used to create Services which take and return Elem objects
    * instead of HTTP responses
    */
  object Filters extends AutoFilters[Elem] {

    override protected val responseBuilder = Xml.ResponseBuilder

    import Xml.ResponseBuilder.implicits._

    private val body = Body.xml(None)

    private def toResponse(successStatus: Status = Ok) = (out: Elem) => successStatus(out)

    /**
      * Wrap the enclosed service with auto-marshalling of input and output Elem instances for HTTP POST scenarios
      * which return an Elem.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOut(svc: Service[Elem, Elem], successStatus: Status = Ok): Service[Request, Response] =
    AutoIn(body).andThen(AutoOut[Elem](successStatus)).andThen(svc)

    /**
      * Wrap the enclosed service with auto-marshalling of input and output Elem instances for HTTP POST scenarios
      * which may return an Elem.
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoInOptionalOut(svc: Service[Elem, Option[Elem]], successStatus: Status = Ok)
    : Service[Request, Response] = _AutoInOptionalOut[Elem, Elem](svc, body, toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of output Elem instances for HTTP scenarios where an object is returned.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoOut[IN](successStatus: Status = Ok): Filter[IN, Response, IN, Elem] = _AutoOut(toResponse(successStatus))

    /**
      * Filter to provide auto-marshalling of Elem instances for HTTP scenarios where an object may not be returned
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoOptionalOut[IN](successStatus: Status = Ok): Filter[IN, Response, IN, Option[Elem]]
    = _AutoOptionalOut(toResponse(successStatus))

  }

  object ResponseBuilder extends AbstractResponseBuilder[Elem] {

    private def format(node: Elem): String = node.toString()

    private def formatErrorMessage(errorMessage: String): Elem = <message>{errorMessage}</message>

    private def formatError(throwable: Throwable): Elem = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[Elem](i => Buf.Utf8(format(i)), formatErrorMessage, formatError, ContentTypes.APPLICATION_XML)
  }

}