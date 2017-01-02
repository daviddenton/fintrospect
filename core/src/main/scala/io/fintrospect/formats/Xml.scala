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
  object Filters {

    import ResponseBuilder._

    type ToResponse[L] = (L) => ResponseBuilder[_]
    type ToBody[BODY] = () => Body[BODY]

    private val body = Body.xml(None)
    private def toResponse(successStatus: Status = Status.Ok) = (out: Elem) => HttpResponse(successStatus).withContent(out)

    def AutoIn(): Filter[Request, Response, Elem, Response] = Filter.mk[Request, Response, Elem, Response] {
      (req, svc) =>
        body <--? req match {
          case Extracted(in) => svc(in.get)
          case ExtractionFailed(_) => HttpResponse(Status.BadRequest)
        }
    }

    def AutoInOut(successStatus: Status = Status.Ok): Filter[Request, Response, Elem, Elem] =
      AutoIn().andThen(AutoOut[Elem](successStatus))

    def AutoInOptionalOut(successStatus: Status = Status.Ok): Filter[Request, Response, Elem, Option[Elem]]
    = AutoIn().andThen(AutoOptionalOut[Elem](successStatus))

    def AutoOut[IN](successStatus: Status = Status.Ok): Filter[IN, Response, IN, Elem]
    = Filter.mk[IN, Response, IN, Elem] { (req, svc) => svc(req).map(t => toResponse(successStatus)(t).build()) }

    def AutoOptionalOut[IN](successStatus: Status = Status.Ok): Filter[IN, Response, IN, Option[Elem]]
    = Filter.mk[IN, Response, IN, Option[Elem]] {
      (req, svc) => svc(req).map(_.map(toResponse(successStatus)).getOrElse(HttpResponse(Status.NotFound)).build())
    }
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