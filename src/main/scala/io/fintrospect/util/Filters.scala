package io.fintrospect.util

import java.nio.charset.StandardCharsets.ISO_8859_1
import java.security.MessageDigest
import java.util.Base64

import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.configuration.{Authority, Credentials}

/**
  * Useful filters
  */
object Filters {

  def addAuthorityHost[T](authority: Authority) = Filter.mk[Request, T, Request, T] {
    (req, svc) => {
      req.headerMap.add("Host", authority.toString)
      svc(req)
    }
  }

  def addBasicAuthorization[T](credentials: Credentials) = Filter.mk[Request, T, Request, T] {
    (req, svc) => {
      val base64Credentials = Base64.getEncoder.encodeToString(s"${credentials.username}:${credentials.password}".getBytes(ISO_8859_1))
      req.headerMap.add("Authorization", "Basic " + base64Credentials.trim)
      svc(req)
    }
  }

  // Stolen from http://stackoverflow.com/questions/26423662/scalatra-response-hmac-calulation
  def addETag[T](): Filter[T, Response, T, Response] = Filter.mk[T, Response, T, Response] {
    (req, svc) => svc(req)
      .map {
        rsp => {
          val hashedBody = MessageDigest.getInstance("MD5").digest(rsp.contentString.getBytes).map("%02x".format(_)).mkString
          rsp.headerMap("ETag") = hashedBody
          rsp
        }
      }
  }

}
