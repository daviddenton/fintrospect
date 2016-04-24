package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.NotAcceptable
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.json.Argo.ResponseBuilder.implicits._
import io.fintrospect.parameters.Header

/**
  * Service which allows multiple content types to be supported on a single route. Note that due to Content-Type
  * negotiation being quite complicated, this service does NOT implement full functionality, such as q values or levels.
  *
  * The implementation is:
  * Check the Accept header and tries to get an exact match on any "<level1>/<level2>" value that it finds. If no match
  * can be found, return an HTTP 406 (Not Acceptable) status. Wildcards or missing Accept headers will choose the first
  * supplied service in the list.
  */
object MultiContentType {
  private val accept = Header.optional.*.string("Accept")

  def apply(services: (ContentType, Service[Request, Response])*): Service[Request, Response] = {

    val accepted =
      (services ++ services.headOption.map(p => ContentTypes.WILDCARD -> p._2))
        .map(p => (p._1.value.toLowerCase, p._2))
        .toMap

    Service.mk {
      request: Request => {
        accept.from(request)
          .map(accepting => {
            accepting
              .flatMap(_.split(Array(',', ' ', ';')))
              .map(_.toLowerCase)
              .filter(_.matches(".+\\/.+"))
              .toSet
              .find(accepted.contains)
              .flatMap(accepted.get)
              .map(_ (request))
              .getOrElse(NotAcceptable().toFuture)
          })
          .getOrElse(
            services
              .headOption
              .map(_._2(request))
              .getOrElse(NotAcceptable().toFuture))
      }
    }
  }
}
