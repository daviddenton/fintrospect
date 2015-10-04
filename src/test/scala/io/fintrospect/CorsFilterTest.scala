package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.{Response, _}
import com.twitter.finagle.httpx.filter.Cors
import com.twitter.util.{Await, Duration}
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo.ResponseBuilder._
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.scalatest.{FlatSpec, MustMatchers}

class CorsFilterTest extends FlatSpec with MustMatchers {
  val underlying = Service.mk[Request, Response] { request =>
    if (request.method.toString().equals("TRAP")) Ok("#guwop") else Error(Status.MethodNotAllowed, "")
  }

  val policy = Cors.Policy(
    allowsOrigin = {
      case origin if origin.startsWith("juug") => Option(origin)
      case origin if origin.endsWith("street") => Option(origin)
      case _ => None
    },
    allowsMethods = { method => Option(method :: "TRAP" :: Nil) },
    allowsHeaders = { headers => Option(headers) },
    exposedHeaders = "Icey" :: Nil,
    supportsCredentials = true,
    maxAge = Option(Duration.Top)
  )

  val corsFilter = new CorsFilter(policy)
  val service = corsFilter andThen underlying

  "Cors.HttpFilter" should "handle preflight requests" in {
    val request = Request()
    request.method = Method.Options
    request.headerMap.set("Origin", "thestreet")
    request.headerMap.set("Access-Control-Request-Method", "BRR")

    val response = Await result service(request)
    response.headerMap.get("Access-Control-Allow-Origin") must be("thestreet")
    response.headerMap.get("Access-Control-Allow-Credentials") must be("true")
    response.headerMap.get("Access-Control-Allow-Methods") must be("BRR, TRAP")
    response.headerMap.get("Vary") must be("Origin")
    response.headerMap.get("Access-Control-Max-Age") must be(Duration.Top.inSeconds.toString)
    contentFrom(response) must be("")
  }

  it should "respond to invalid preflight requests without CORS headers" in {
    val request = Request()
    request.method = Method.Options

    val response = Await result service(request)
    response.status must be(Status.Ok)
    response.headerMap.get("Access-Control-Allow-Origin") must be(null)
    response.headerMap.get("Access-Control-Allow-Credentials") must be(null)
    response.headerMap.get("Access-Control-Allow-Methods") must be(null)
    response.headerMap.get("Vary") must be("Origin")
    contentFrom(response) must be("")
  }

  it should "respond to unacceptable cross-origin requests without CORS headers" in {
    val request = Request()
    request.method = Method.Options
    request.headerMap.set("Origin", "theclub")

    val response = Await result service(request)
    response.status must be(Status.Ok)
    response.headerMap.get("Access-Control-Allow-Origin") must be(null)
    response.headerMap.get("Access-Control-Allow-Credentials") must be(null)
    response.headerMap.get("Access-Control-Allow-Methods") must be(null)
    response.headerMap.get("Vary") must be("Origin")
    contentFrom(response) must be("")
  }

  it should "handle simple requests" in {
    val request = Request()
//    request.setM =
    request.headerMap.set("Origin", "juughaus")

    val response = Await result service(request)
    response.headerMap.get("Access-Control-Allow-Origin") must be("juughaus")
    response.headerMap.get("Access-Control-Allow-Credentials") must be("true")
    response.headerMap.get("Access-Control-Expose-Headers") must be("Icey")
    response.headerMap.get("Vary") must be("Origin")
    contentFrom(response) must be("#guwop")
  }

  it should "not add response headers to simple requests if request headers aren't present" in {
    val request = Request()
//    request.method = TRAP

    val response = Await result service(request)
    response.headerMap.get("Access-Control-Allow-Origin") must be(null)
    response.headerMap.get("Access-Control-Allow-Credentials") must be(null)
    response.headerMap.get("Access-Control-Expose-Headers") must be(null)
    response.headerMap.get("Vary") must be("Origin")
    contentFrom(response) must be("#guwop")
  }
}