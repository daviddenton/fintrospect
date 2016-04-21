package io.fintrospect.util

import java.nio.charset.StandardCharsets.ISO_8859_1
import java.security.MessageDigest
import java.time.{Clock, Duration}
import java.util.Base64

import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.Headers
import io.fintrospect.configuration.{Authority, Credentials}

/**
  * Useful filters
  */
object Filters {

  def addAuthorityHost[T](authority: Authority) = Filter.mk[Request, T, Request, T] {
    (req, svc) => {
      Headers.Host.of(authority.toString)(req)
      svc(req)
    }
  }

  def addBasicAuthorization[T](credentials: Credentials) = Filter.mk[Request, T, Request, T] {
    (req, svc) => {
      val base64Credentials = Base64.getEncoder.encodeToString(s"${credentials.username}:${credentials.password}".getBytes(ISO_8859_1))
      Headers.Authorization.of("Basic " + base64Credentials.trim)(req)
      svc(req)
    }
  }

  // Stolen from http://stackoverflow.com/questions/26423662/scalatra-response-hmac-calulation
  def addETag[T](): Filter[T, Response, T, Response] = Filter.mk[T, Response, T, Response] {
    (req, svc) => svc(req)
      .map {
        rsp => {
          val hashedBody = MessageDigest.getInstance("MD5").digest(rsp.contentString.getBytes).map("%02x".format(_)).mkString
          Headers.ETag.of(hashedBody)(rsp)
          rsp
        }
      }
  }

  def reportingPathAndLatency(clock: Clock)(recordFn: (String, Duration) => Unit) = Filter.mk[Request, Response, Request, Response] {
    (req, svc) => {
      val start = clock.instant()
      for {
        resp <- svc(req)
      } yield {
        val identifier = List(
          (Headers.IdentifyRouteName <-- req)
            .map(_.replace('.', '_').replace(':', '.'))
            .getOrElse(req.method.toString() + ".UNMAPPED")
            .replace('/', '_'),
          resp.status.code / 100 + "xx",
          resp.status.code.toString).mkString(".")

        recordFn(identifier, Duration.between(start, clock.instant()))
        resp
      }
    }
  }

}
