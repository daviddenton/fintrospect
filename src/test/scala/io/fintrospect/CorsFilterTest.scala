package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.filter.Cors
import com.twitter.util.{Await, Duration}
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import io.fintrospect.util.JsonResponseBuilder.{Error, Ok}
import io.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse, HttpResponseStatus}
import org.scalatest.{FlatSpec, MustMatchers}

class CorsFilterTest extends FlatSpec with MustMatchers {
  val TRAP = new HttpMethod("TRAP")
  val underlying = Service.mk[HttpRequest, HttpResponse] { request =>
    if (request.getMethod == TRAP) Ok("#guwop") else Error(HttpResponseStatus.METHOD_NOT_ALLOWED, "")
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
    request.method = HttpMethod.OPTIONS
    request.headers().set("Origin", "thestreet")
    request.headers().set("Access-Control-Request-Method", "BRR")

    val response = Await result service(request)
    response.headers().get("Access-Control-Allow-Origin") must be("thestreet")
    response.headers().get("Access-Control-Allow-Credentials") must be("true")
    response.headers().get("Access-Control-Allow-Methods") must be("BRR, TRAP")
    response.headers().get("Vary") must be("Origin")
    response.headers().get("Access-Control-Max-Age") must be(Duration.Top.inSeconds.toString)
    contentFrom(response) must be("")
  }

  it should "respond to invalid preflight requests without CORS headers" in {
    val request = Request()
    request.method = HttpMethod.OPTIONS

    val response = Await result service(request)
    response.getStatus must be(HttpResponseStatus.OK)
    response.headers().get("Access-Control-Allow-Origin") must be(null)
    response.headers().get("Access-Control-Allow-Credentials") must be(null)
    response.headers().get("Access-Control-Allow-Methods") must be(null)
    response.headers().get("Vary") must be("Origin")
    contentFrom(response) must be("")
  }

  it should "respond to unacceptable cross-origin requests without CORS headers" in {
    val request = Request()
    request.method = HttpMethod.OPTIONS
    request.headers().set("Origin", "theclub")

    val response = Await result service(request)
    response.getStatus must be(HttpResponseStatus.OK)
    response.headers().get("Access-Control-Allow-Origin") must be(null)
    response.headers().get("Access-Control-Allow-Credentials") must be(null)
    response.headers().get("Access-Control-Allow-Methods") must be(null)
    response.headers().get("Vary") must be("Origin")
    contentFrom(response) must be("")
  }

  it should "handle simple requests" in {
    val request = Request()
    request.method = TRAP
    request.headers().set("Origin", "juughaus")

    val response = Await result service(request)
    response.headers().get("Access-Control-Allow-Origin") must be("juughaus")
    response.headers().get("Access-Control-Allow-Credentials") must be("true")
    response.headers().get("Access-Control-Expose-Headers") must be("Icey")
    response.headers().get("Vary") must be("Origin")
    contentFrom(response) must be("#guwop")
  }

  it should "not add response headers to simple requests if request headers aren't present" in {
    val request = Request()
    request.method = TRAP

    val response = Await result service(request)
    response.headers().get("Access-Control-Allow-Origin") must be(null)
    response.headers().get("Access-Control-Allow-Credentials") must be(null)
    response.headers().get("Access-Control-Expose-Headers") must be(null)
    response.headers().get("Vary") must be("Origin")
    contentFrom(response) must be("#guwop")
  }
}