package io.fintrospect.formats

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.parameters.{Body, Mandatory}
import io.fintrospect.util.{Extracted, ExtractionFailed}

trait ToOut[T, O] {
  def apply(t: T): O
}

class NuAutoFilters[T](val responseBuilder: AbstractResponseBuilder[T]) {

  import responseBuilder._

  private def toResponse[OUT](successStatus: Status, toOut: ToOut[OUT, T]): (OUT) => Response =
    (t: OUT) => HttpResponse(successStatus).withContent(toOut(t)).build()

  def AutoIn[IN](body: Body[IN] with Mandatory[Request, IN]): Filter[Request, Response, IN, Response] = {
    Filter.mk[Request, Response, IN, Response] {
      (req, svc) =>
        body <--? req match {
          case Extracted(in) => svc(in.get)
          case ExtractionFailed(_) => BadRequest("malformed body")
        }
    }
  }

  def AutoOut[IN, OUT](successStatus: Status = Status.Ok)
                      (implicit toOut: ToOut[OUT, T]): Filter[IN, Response, IN, OUT]
  = Filter.mk[IN, Response, IN, OUT] { (req, svc) => svc(req).map(toResponse(successStatus, toOut)) }

  def AutoInOut[IN, OUT](successStatus: Status = Status.Ok)
                          (implicit body: Body[IN] with Mandatory[Request, IN], toOut: ToOut[OUT, T], example: IN = null)
  : Filter[Request, Response, IN, OUT]
   = AutoIn(body).andThen(AutoOut(successStatus)(toOut))


  def AutoInOptionalOut[IN, OUT](successStatus: Status = Status.Ok)
                                  (implicit body: Body[IN] with Mandatory[Request, IN], toOut: ToOut[OUT, T], example: IN = null)
  : Filter[Request, Response, IN, Option[OUT]] = AutoIn(body).andThen(AutoOptionalOut(successStatus)(toOut))

  def AutoOptionalOut[IN, OUT](successStatus: Status = Status.Ok)
                              (implicit toOut: ToOut[OUT, T]): Filter[IN, Response, IN, Option[OUT]] = {
    Filter.mk[IN, Response, IN, Option[OUT]] {
      (req, svc) => svc(req).map(_.map(toResponse(successStatus, toOut)).getOrElse(NotFound("").build()))
    }
  }
}
