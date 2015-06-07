package io.fintrospect.clients

import com.twitter.finagle.Service
import com.twitter.io.Charsets
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.parameters.Path
import io.fintrospect.util.PlainTextResponseBuilder
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse, HttpResponseStatus}
import org.scalatest.{FunSpec, ShouldMatchers}

class ClientTest extends FunSpec with ShouldMatchers {

  describe("Client") {
    val returnsMethodAndUri = Service.mk[HttpRequest, HttpResponse] { request =>
      Future.value(PlainTextResponseBuilder.Ok(request.getMethod + "," + request.getUri))
    }
    val name = Path.integer("name")
    val maxAge = Path.integer("maxAge")
    val clientWithNoParameters = new Client(GET, Nil, Nil, returnsMethodAndUri)
    val clientWithNameAndMaxAge = new Client(GET, Nil, List(name, maxAge), returnsMethodAndUri)

    describe("invalid parameters") {
      it("missing parameters throw up") {
        result(clientWithNameAndMaxAge()).getStatus shouldEqual HttpResponseStatus.BAD_REQUEST
      }
      it("unknown parameters returns bad request") {
        result(clientWithNoParameters(maxAge -> "7")).getStatus shouldEqual HttpResponseStatus.BAD_REQUEST
      }
    }
    describe("converts the path parameters into the correct url") {
      it("when there are none") {
        val response = result(clientWithNoParameters())
        response.getStatus shouldEqual HttpResponseStatus.OK
        response.getContent.toString(Charsets.Utf8) shouldEqual "GET,"
      }
      it("when there are some") {
        val response = result(clientWithNameAndMaxAge(maxAge -> "7", name -> "bob"))
        response.getStatus shouldEqual HttpResponseStatus.OK
        response.getContent.toString(Charsets.Utf8) shouldEqual "GET,bob/7"
      }
      it("ignores fixed") {
        val clientWithFixedSections = new Client(GET, Nil, List(Path.fixed("prefix"), maxAge, Path.fixed("suffix")), returnsMethodAndUri)
        val response = result(clientWithFixedSections(maxAge -> "7"))
        response.getStatus shouldEqual HttpResponseStatus.OK
        response.getContent.toString(Charsets.Utf8) shouldEqual "GET,prefix/7/suffix"
      }
    }

  }

}
