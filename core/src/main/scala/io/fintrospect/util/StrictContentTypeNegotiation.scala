package io.fintrospect.util

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.NotAcceptable
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.Argo.ResponseBuilder.implicits._
import io.fintrospect.{ContentType, ContentTypes}

/**
  * Service which allows strict content type negotiation (for multiple mappings) to be supported on a single route. Note that due to Content-Type
  * negotiation being quite complicated, this service does NOT support full functionality, such as q values or levels.
  *
  * The implementation is:
  * Check the Accept header and tries to get an exact match on any "<level1>/<level2>" value that it finds. If no match
  * can be found, return an HTTP 406 (Not Acceptable) status. Wildcards or missing Accept headers will choose the first
  * supplied service in the list.
  */

object StrictContentTypeNegotiation {
  private def matching[T](req: Request, services: Seq[(ContentType, T)]): Option[T] = {
    val accepted = ContentType.fromAcceptHeaders(req).getOrElse(Set(ContentTypes.WILDCARD))

    val contentTypesToServices = services.toMap

    accepted.find(contentTypesToServices.contains)
      .map(contentTypesToServices)
      .orElse(if (accepted.contains(ContentTypes.WILDCARD)) services.headOption.map(_._2) else None)
  }

  private val notAcceptable = Service.constant(NotAcceptable())

  implicit def apply(mappings: (ContentType, Service[Request, Response])*): Service[Request, Response] =
    Service.mk[Request, Response] { req => matching(req, mappings).getOrElse(notAcceptable)(req) }

  implicit def apply[A](mappings: (ContentType, A => Service[Request, Response])*): A => Service[Request, Response] =
    (a: A) => Service.mk[Request, Response] { req => matching(req, mappings).map(_ (a)).getOrElse(notAcceptable)(req) }

  implicit def apply[A, B](mappings: (ContentType, (A, B) => Service[Request, Response])*): (A, B) => Service[Request, Response] =
    (a: A, b: B) => Service.mk[Request, Response] { req => matching(req, mappings).map(_ (a, b)).getOrElse(notAcceptable)(req) }

  implicit def apply[A, B, C](mappings: (ContentType, (A, B, C) => Service[Request, Response])*): (A, B, C) => Service[Request, Response] =
    (a: A, b: B, c: C) => Service.mk[Request, Response] { req => matching(req, mappings).map(_ (a, b, c)).getOrElse(notAcceptable)(req) }

  implicit def apply[A, B, C, D](mappings: (ContentType, (A, B, C, D) => Service[Request, Response])*): (A, B, C, D) => Service[Request, Response] =
    (a: A, b: B, c: C, d: D) => Service.mk[Request, Response] { req => matching(req, mappings).map(_ (a, b, c, d)).getOrElse(notAcceptable)(req) }

  implicit def apply[A, B, C, D, E](mappings: (ContentType, (A, B, C, D, E) => Service[Request, Response])*): (A, B, C, D, E) => Service[Request, Response] =
    (a: A, b: B, c: C, d: D, e: E) => Service.mk[Request, Response] { req => matching(req, mappings).map(_ (a, b, c, d, e)).getOrElse(notAcceptable)(req) }

  implicit def apply[A, B, C, D, E, F](mappings: (ContentType, (A, B, C, D, E, F) => Service[Request, Response])*): (A, B, C, D, E, F) => Service[Request, Response] =
    (a: A, b: B, c: C, d: D, e: E, f: F) => Service.mk[Request, Response] { req => matching(req, mappings).map(_ (a, b, c, d, e, f)).getOrElse(notAcceptable)(req) }

  implicit def apply[A, B, C, D, E, F, G](mappings: (ContentType, (A, B, C, D, E, F, G) => Service[Request, Response])*): (A, B, C, D, E, F, G) => Service[Request, Response] =
    (a: A, b: B, c: C, d: D, e: E, f: F, g: G) => Service.mk[Request, Response] { req => matching(req, mappings).map(_ (a, b, c, d, e, f, g)).getOrElse(notAcceptable)(req) }

}
