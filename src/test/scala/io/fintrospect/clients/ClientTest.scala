package io.fintrospect.clients

import com.twitter.finagle.Service
import com.twitter.io.Charsets
import com.twitter.util.Await._
import com.twitter.util.Future
import io.fintrospect.parameters.Path
import io.fintrospect.util.PlainTextResponseBuilder
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse, HttpResponseStatus}
import org.scalatest.{FunSpec, ShouldMatchers}

class ClientTest extends FunSpec with ShouldMatchers {

  describe("Client") {
    val returnsMethodAndUri = Service.mk[HttpRequest, HttpResponse] { request =>
      Future.value(PlainTextResponseBuilder.Ok(request.getMethod + "," + request.getUri))
    }
    val name = Path.string("name")
    val maxAge = Path.integer("maxAge")
    val clientWithNoParameters = new Client(GET, Nil, Nil, returnsMethodAndUri)
    val clientWithNameAndMaxAge = new Client(GET, Nil, List(name, maxAge), returnsMethodAndUri)

    describe("invalid parameters") {
      it("missing parameters throw up") {
        responseFor(clientWithNameAndMaxAge()) shouldEqual(BAD_REQUEST, "Client: Missing required params passed: List({name}, {maxAge})")
      }
      it("unknown parameters returns bad request") {
        responseFor(clientWithNoParameters(maxAge -> "7")) shouldEqual(BAD_REQUEST, "Client: Illegal params passed: Set({maxAge})")
      }
    }

    describe("converts the path parameters into the correct url") {
      it("when there are none") {
        responseFor(clientWithNoParameters()) shouldEqual(OK, "GET,")
      }
      it("when there are some") {
        responseFor(clientWithNameAndMaxAge(maxAge -> "7", name -> "bob")) shouldEqual(OK, "GET,bob/7")
      }
      it("ignores fixed") {
        val clientWithFixedSections = new Client(GET, Nil, List(Path.fixed("prefix"), maxAge, Path.fixed("suffix")), returnsMethodAndUri)
        responseFor(clientWithFixedSections(maxAge -> "7")) shouldEqual(OK, "GET,prefix/7/suffix")
      }
    }

//    describe("converts the query parameters into the correct url format") {
//      val clientWithNameQuery = new Client(GET,
//        List(Query.optional.string("name")),
//        List(Path.fixed("prefix")), returnsMethodAndUri)
//
//      it("when there are some") {
//        responseFor(clientWithNameQuery(name -> "bob")) shouldEqual(OK, "GET,prefix/?name=bob")
//      }
//      it("optional query params are ignored if not there") {
//        responseFor(clientWithNameQuery(name -> "bob")) shouldEqual(OK, "GET,prefix")
//      }
//    }

  }

  def responseFor(future: Future[HttpResponse]): (HttpResponseStatus, String) = {
    val response = result(future)
    (response.getStatus, response.getContent.toString(Charsets.Utf8))
  }

}
