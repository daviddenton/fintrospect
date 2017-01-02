package io.fintrospect.formats

import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request, Response, Status}
import io.fintrospect.parameters.{Body, Mandatory}
import io.fintrospect.util.{Extracted, ExtractionFailed}

class AutoFilters[R](responseBuilder: AbstractResponseBuilder[R]) {

  type SvcBody[IN] = Body[IN] with Mandatory[Request, IN]
  type Transform[IN, OUT] = (IN => OUT)

  import responseBuilder._

  /**
    * Filter to provide auto-marshalling of input case class instances for scenarios where an object is the service input.
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def AutoIn[IN](body: SvcBody[IN]): Filter[Request, Response, IN, Response] =
    Filter.mk[Request, Response, IN, Response] {
      (req, svc) =>
        body <--? req match {
          case Extracted(in) => svc(in.get)
          case ExtractionFailed(_) => HttpResponse(Status.BadRequest)
        }
    }

  /**
    * Filter to provide auto-marshalling of output case class instances for scenarios where an object is service output.
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def AutoOut[OUT](successStatus: Status = Status.Ok)
                  (implicit transform: Transform[OUT, R]): Filter[Request, Response, Request, OUT]
  = Filter.mk[Request, Response, Request, OUT] { (req, svc) =>
    svc(req)
      .map(transform)
      .map(l => HttpResponse(successStatus).withContent(l))
  }

  /**
    * Filter to provide auto-marshalling of case class instances for input and output to the service
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def AutoInOut[IN, OUT](body: SvcBody[IN], successStatus: Status = Status.Ok)
                        (implicit transform: Transform[OUT, R]): Filter[Request, Response, IN, OUT]
  = AutoIn(body).andThen(Filter.mk[IN, Response, IN, OUT] { (req, svc) =>
    svc(req)
      .map(transform)
      .map(l => HttpResponse(successStatus).withContent(l))
  })


  /**
    * Filter to provide auto-marshalling of case class instances for scenarios where an object may not be returned from the service
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def AutoInOptionalOut[IN, OUT](body: SvcBody[IN], successStatus: Status = Status.Ok)
                                (implicit transform: Transform[OUT, R]): Filter[Request, Response, IN, Option[OUT]]
  = AutoIn(body).andThen(AutoOptionalOut(successStatus)(transform))

  /**
    * Filter to provide auto-marshalling of output case class instances for scenarios where an object may not be returned from the service
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def AutoOptionalOut[IN, OUT](successStatus: Status = Status.Ok)
                              (implicit transform: Transform[OUT, R]): Filter[IN, Response, IN, Option[OUT]] =
    Filter.mk[IN, Response, IN, Option[OUT]] {
      (req, svc) =>
        svc(req)
          .map(
            _.map(transform)
              .map(l => HttpResponse(successStatus).withContent(l))
              .getOrElse(HttpResponse(Status.NotFound)))
          .map(_.build())
    }
}
