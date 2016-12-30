package io.fintrospect.formats

import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request, Response, Status}
import io.fintrospect.parameters.{Body, Mandatory}
import io.fintrospect.util.{Extracted, ExtractionFailed}

class NuAutoFilters[T](val responseBuilder: AbstractResponseBuilder[T]) {

  type InB[IN] = Body[IN] with Mandatory[Request, IN]
  type ToOut[IN, OUT] = (IN => OUT)

  import responseBuilder._

  private def toResponse[OUT](status: Status, toOut: ToOut[OUT, T]): (OUT) => Response =
    (t: OUT) => HttpResponse(status).withContent(toOut(t)).build()

  def AutoIn[IN](body: InB[IN]): Filter[Request, Response, IN, Response] =
    Filter.mk[Request, Response, IN, Response] {
      (req, svc) =>
        body <--? req match {
          case Extracted(in) => svc(in.get)
          case ExtractionFailed(_) => BadRequest("malformed body")
        }
    }

  def AutoOut[OUT](successStatus: Status = Status.Ok)
                  (implicit toOut: ToOut[OUT, T]): Filter[Request, Response, Request, OUT]
  = Filter.mk[Request, Response, Request, OUT] { (req, svc) => svc(req).map(toResponse(successStatus, toOut)) }

  def AutoInOut[IN, OUT](body: InB[IN], successStatus: Status = Status.Ok)
                        (implicit toOut: ToOut[OUT, T]): Filter[Request, Response, IN, OUT]
  = AutoIn(body).andThen(Filter.mk[IN, Response, IN, OUT] { (req, svc) => svc(req).map(toResponse(successStatus, toOut)) })


  def AutoInOptionalOut[IN, OUT](body: InB[IN], successStatus: Status = Status.Ok)
                                (implicit toOut: ToOut[OUT, T]): Filter[Request, Response, IN, Option[OUT]]
  = AutoIn(body).andThen(AutoOptionalOut(successStatus)(toOut))

  def AutoOptionalOut[IN, OUT](successStatus: Status = Status.Ok)
                              (implicit toOut: ToOut[OUT, T]): Filter[IN, Response, IN, Option[OUT]] =
    Filter.mk[IN, Response, IN, Option[OUT]] {
      (req, svc) =>
        svc(req)
          .map(_.map(toResponse(successStatus, toOut))
            .getOrElse(NotFound("").build()))
    }
}
