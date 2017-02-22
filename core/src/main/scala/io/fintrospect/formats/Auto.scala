package io.fintrospect.formats

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.parameters.{Body, Mandatory}
import io.fintrospect.util.{Extracted, ExtractionFailed}

class Auto[R](responseBuilder: AbstractResponseBuilder[R]) {

  type SvcBody[IN] = Body[IN] with Mandatory[Request, IN]

  import responseBuilder._

  /**
    * Service wrapper to provide auto-marshalling of input case class instances for scenarios where an object is the service input.
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def In[IN](svc: Service[IN, Response])(implicit body: Body[IN]): Service[Request, Response] =
    Filter.mk[Request, Response, IN, Response] {
      (req, svc) =>
        body <--? req match {
          case Extracted(in) => svc(in)
          case ExtractionFailed(e) => HttpResponse(Status.BadRequest).withErrorMessage(s"Failed to unmarshal body [${e.mkString(", ")}]")
        }
    }.andThen(svc)

  /**
    * Service wrapper to provide auto-marshalling of output case class instances for scenarios where an object is service output.
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def Out[OUT](svc: Service[Request, OUT], successStatus: Status = Status.Ok)
              (implicit transform: OUT => R): Service[Request, Response] = Filter.mk[Request, Response, Request, OUT] {
    (req, svc) =>
      svc(req)
        .map(transform)
        .map(l => HttpResponse(successStatus).withContent(l))
  }.andThen(svc)


  /**
    * Service wrapper to provide auto-marshalling of case class instances for input and output to the service
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def InOut[IN, OUT](svc: Service[IN, OUT], successStatus: Status = Status.Ok)
                    (implicit body: Body[IN], transform: OUT => R): Service[Request, Response] = In(Filter.mk[IN, Response, IN, OUT] {
    (req, svc) =>
      svc(req)
        .map(transform)
        .map((l: R) => HttpResponse(successStatus).withContent(l))
  }.andThen(svc))(body)

  /**
    * Service wrapper to provide auto-marshalling of case class instances for scenarios where an object may not be returned from the service
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def InOptionalOut[IN, OUT](svc: Service[IN, Option[OUT]], successStatus: Status = Status.Ok)
                            (implicit body: Body[IN], transform: OUT => R): Service[Request, Response]
  = In(OptionalOut(svc, successStatus)(transform))(body)

  /**
    * Service wrapper to provide auto-marshalling of output case class instances for scenarios where an object may not be returned from the service
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def OptionalOut[IN, OUT](svc: Service[IN, Option[OUT]], successStatus: Status = Status.Ok)
                          (implicit transform: OUT => R): Service[IN, Response] = Filter.mk[IN, Response, IN, Option[OUT]] {
    (req, svc) =>
      svc(req)
        .map(
          _.map(transform)
            .map(l => HttpResponse(successStatus).withContent(l))
            .getOrElse(HttpResponse(Status.NotFound).withErrorMessage("No object available to unmarshal")))
        .map(_.build())
  }.andThen(svc)
}
