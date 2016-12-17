package io.fintrospect.formats

import com.twitter.finagle.http.Status.{BadRequest, NotFound}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.parameters.{Body, Mandatory}
import io.fintrospect.util.{Extracted, ExtractionFailed}

trait AutoFilters[T] {

  protected val responseBuilder: AbstractResponseBuilder[T]

  import responseBuilder.implicits._

  type ToResponse[L] = (L) => ResponseBuilder[_]
  type ToBody[BODY] = () => Body[BODY]

  def AutoIn[IN](body: Body[IN] with Mandatory[Request, IN]) = Filter.mk[Request, Response, IN, Response] {
    (req, svc) =>
      body <--? req match {
        case Extracted(in) => svc(in.get)
        case ExtractionFailed(_) => BadRequest("malformed body")
    }
  }

  def _AutoOut[IN, OUT](fn: ToResponse[OUT]): Filter[IN, Response, IN, OUT] =
    Filter.mk[IN, Response, IN, OUT] { (req, svc) => svc(req).map(t => fn(t).build()) }

  def _AutoInOptionalOut[BODY, OUT](svc: Service[BODY, Option[OUT]], body: Body[BODY] with Mandatory[Request, BODY], toResponse: ToResponse[OUT]): Service[Request, Response] =
    AutoIn[BODY](body).andThen(_AutoOptionalOut[BODY, OUT](toResponse)).andThen(svc)

  def _AutoOptionalOut[IN, OUT](success: ToResponse[OUT]): Filter[IN, Response, IN, Option[OUT]] =
    Filter.mk[IN, Response, IN, Option[OUT]] {
      (req, svc) => svc(req).map(optT => optT.map(t => success(t).build()).getOrElse(NotFound().build()))
    }
}
