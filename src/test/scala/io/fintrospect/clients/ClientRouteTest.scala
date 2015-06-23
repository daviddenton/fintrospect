package io.fintrospect.clients

import com.twitter.finagle.Service
import com.twitter.util.Await._
import com.twitter.util.Future
import io.fintrospect.parameters.{Header, Path}
import io.fintrospect.util.HttpRequestResponseUtil._
import io.fintrospect.util.PlainTextResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse, HttpResponseStatus}
import org.scalatest.{FunSpec, ShouldMatchers}

class ClientRouteTest extends FunSpec with ShouldMatchers {

  describe("ClientRoute") {
    val returnsMethodAndUri = Service.mk[HttpRequest, HttpResponse] { request =>
      Future.value(Ok(request.getMethod + "," + request.getUri))
    }
    val name = Path.string("name")
    val maxAge = Path.integer("maxAge")
    val clientWithNoParameters = ClientRoute().at(GET) bindTo returnsMethodAndUri

    val clientWithNameAndMaxAge = ClientRoute().at(GET) / name / maxAge bindTo returnsMethodAndUri
//
//    describe("invalid parameters") {
//      it("missing parameters throw up") {
//        responseFor(clientWithNameAndMaxAge()) shouldEqual(BAD_REQUEST, "Client: Missing required params passed: List({name}, {maxAge})")
//      }
//      it("unknown parameters returns bad request") {
//        responseFor(clientWithNoParameters(maxAge -> 7)) shouldEqual(BAD_REQUEST, "Client: Illegal params passed: Set({maxAge})")
//      }
//    }
//
//    describe("converts the path parameters into the correct url") {
//      it("when there are none") {
//        responseFor(clientWithNoParameters()) shouldEqual(OK, "GET,")
//      }
//      it("when there are some") {
//        responseFor(clientWithNameAndMaxAge(maxAge -> 7, name.->("bob"))) shouldEqual(OK, "GET,bob/7")
//      }
//      it("ignores fixed") {
//        val clientWithFixedSections = ClientRoute().at(GET) / "prefix" / maxAge / "suffix" bindTo returnsMethodAndUri
//        responseFor(clientWithFixedSections(maxAge -> 7)) shouldEqual(OK, "GET,prefix/7/suffix")
//      }
//    }
//
//    describe("converts the query parameters into the correct url format") {
//      val nameQuery = Query.optional.string("name")
//      val clientWithNameQuery = ClientRoute().taking(nameQuery).at(GET) / "prefix" bindTo returnsMethodAndUri
//
//      it("when there are some") {
//        responseFor(clientWithNameQuery(nameQuery -> "bob")) shouldEqual(OK, "GET,/prefix?name=bob")
//      }
//      it("optional query params are ignored if not there") {
//        responseFor(clientWithNameQuery()) shouldEqual(OK, "GET,/prefix")
//      }
//    }

    describe("puts the header parameters into the request") {
      val returnsHeaders = Service.mk[HttpRequest, HttpResponse] { request => Future.value(Ok(headersFrom(request).toString())) }

      val nameHeader = Header.optional.string("name")

      val clientWithNameHeader = ClientRoute().taking(nameHeader).at(GET) bindTo returnsHeaders

      it("when there are some, includes them") {
        responseFor(clientWithNameHeader(nameHeader -> "bob")) shouldEqual(OK, "Map(name -> bob, X-Fintrospect-Route-Name -> GET.)")
      }
      it("optional query params are ignored if not there") {
        responseFor(clientWithNameHeader()) shouldEqual(OK, "Map(X-Fintrospect-Route-Name -> GET.)")
      }
    }

    describe("identifies") {
      val returnsHeaders = Service.mk[HttpRequest, HttpResponse] { request => Future.value(Ok(headersFrom(request).toString())) }

      val intParam = Path.int("anInt")

      val client = ClientRoute().at(GET) / "svc" / intParam / Path.fixed("fixed") bindTo returnsHeaders

      it("identifies called route as a request header") {
        responseFor(client(intParam -> 55)) shouldEqual (HttpResponseStatus.OK, "Map(X-Fintrospect-Route-Name -> GET.svc/{anInt}/fixed)")
      }
    }
  }

  def responseFor(future: Future[HttpResponse]): (HttpResponseStatus, String) = statusAndContentFrom(result(future))

}
