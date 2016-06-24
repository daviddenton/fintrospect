package io.fintrospect.formats

import com.twitter.finagle.http.Status.NotFound
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.parameters.Body

class AutoFilters[T](protected val responseBuilder: AbstractResponseBuilder[T]) {

  import responseBuilder.implicits._

  type ToResponse[OUT] = (OUT) => ResponseBuilder[_]
  type ToBody[BODY] = () => Body[BODY]

  /**
    * Filter to provide auto-marshalling of input case class instances for HTTP POST scenarios
    */
  def AutoIn[IN, OUT](body: Body[IN]) = Filter.mk[Request, OUT, IN, OUT] { (req, svc) => svc(body <-- req) }

  def _AutoOut[IN, OUT](fn: ToResponse[OUT]) =
    Filter.mk[IN, Response, IN, OUT] { (req, svc) => svc(req).map(t => fn(t).build()) }

  def _AutoInOptionalOut[BODY, OUT](svc: Service[BODY, Option[OUT]], body: Body[BODY], toResponse: ToResponse[OUT]) =
    AutoIn[BODY, Response](body).andThen(_AutoOptionalOut[BODY, OUT](toResponse)).andThen(svc)

  def _AutoOptionalOut[IN, OUT](success: ToResponse[OUT]) =
    Filter.mk[IN, Response, IN, Option[OUT]] {
      (req, svc) => svc(req).map(optT => optT.map(t => success(t).build()).getOrElse(NotFound().build()))
    }
}
