package io.fintrospect.formats

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf
import io.fintrospect.ContentTypes
import io.fintrospect.parameters.{Body, Mandatory}
import io.fintrospect.util.{Extracted, ExtractionFailed}

import scala.xml.Elem

/**
  * Native (Elem-based) Xml support (application/xml content type)
  */
object Xml {

  /**
    * Auto-marshalling filters which can be used to create Services which take and return Elem objects
    * instead of HTTP responses
    */
  object Auto {

    import ResponseBuilder._

    type ToResponse[L] = (L) => ResponseBuilder[_]
    type ToBody[BODY] = () => Body[BODY]

    private val body = Body.xml(None)
    private def toResponse(successStatus: Status = Status.Ok) = (out: Elem) => HttpResponse(successStatus).withContent(out)

    def In(svc: Service[Elem, Response]): Service[Request, Response] = Filter.mk[Request, Response, Elem, Response] {
      (req, svc) =>
        body <--? req match {
          case Extracted(in) => svc(in.get)
          case ExtractionFailed(_) => HttpResponse(Status.BadRequest)
        }
    }.andThen(svc)

    def InOut(svc: Service[Elem, Elem], successStatus: Status = Status.Ok): Service[Request, Response] =
      In(AutoOut[Elem](svc, successStatus))

    def InOptionalOut(svc: Service[Elem, Option[Elem]], successStatus: Status = Status.Ok): Service[Request, Response]
    = In(OptionalOut(svc, successStatus))

    def AutoOut[IN](svc: Service[IN, Elem], successStatus: Status = Status.Ok): Service[IN, Response]
    = Filter.mk[IN, Response, IN, Elem] { (req, svc) => svc(req).map(t => toResponse(successStatus)(t).build()) }.andThen(svc)

    def OptionalOut[IN](svc: Service[IN, Option[Elem]], successStatus: Status = Status.Ok): Service[IN, Response]
    = Filter.mk[IN, Response, IN, Option[Elem]] {
      (req, svc) => svc(req).map(_.map(toResponse(successStatus)).getOrElse(HttpResponse(Status.NotFound)).build())
    }.andThen(svc)
  }

  object ResponseBuilder extends AbstractResponseBuilder[Elem] {

    private def format(node: Elem): String = node.toString()

    private def formatErrorMessage(errorMessage: String): Elem = <message>
      {errorMessage}
    </message>

    private def formatError(throwable: Throwable): Elem = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[Elem](i => Buf.Utf8(format(i)), formatErrorMessage, formatError, ContentTypes.APPLICATION_XML)
  }

}