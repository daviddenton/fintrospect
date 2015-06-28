package io.fintrospect.clients

import com.twitter.finagle.Service
import com.twitter.util.Await._
import com.twitter.util.Future
import io.fintrospect.parameters.{Header, Path, Query}
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
    val gender = Path.string("gender")
    val clientWithNoParameters = ClientRoute().at(GET) bindTo returnsMethodAndUri

    val clientWithNameAndMaxAgeAndGender = ClientRoute().at(GET) / name / maxAge / gender bindTo returnsMethodAndUri

    describe("invalid parameters") {
      it("missing parameters throw up") {
        responseFor(clientWithNameAndMaxAgeAndGender()) shouldEqual(BAD_REQUEST, "Client: Missing required params passed: Set({name}, {maxAge}, {gender})")
      }
      it("unknown parameters returns bad request") {
        responseFor(clientWithNoParameters(maxAge.of(7))) shouldEqual(BAD_REQUEST, "Client: Unknown params passed: Set({maxAge})")
      }
    }

    describe("converts the path parameters into the correct url") {
      it("when there are none") {
        responseFor(clientWithNoParameters()) shouldEqual(OK, "GET,/")
      }
      it("when there are some") {
        responseFor(clientWithNameAndMaxAgeAndGender(gender --> "male", maxAge --> 7, name.-->("bob"))) shouldEqual(OK, "GET,/bob/7/male")
      }
      it("ignores fixed") {
        val clientWithFixedSections = ClientRoute().at(GET) / "prefix" / maxAge / "suffix" bindTo returnsMethodAndUri
        responseFor(clientWithFixedSections(maxAge --> 7)) shouldEqual(OK, "GET,/prefix/7/suffix")
      }
    }

    describe("converts the query parameters into the correct url format") {
      val nameQuery = Query.optional.string("name")
      val clientWithNameQuery = ClientRoute().taking(nameQuery).at(GET) / "prefix" bindTo returnsMethodAndUri

      it("when there are some") {
        responseFor(clientWithNameQuery(nameQuery --> "bob")) shouldEqual(OK, "GET,/prefix?name=bob")
      }
      it("optional query params are ignored if not there") {
        responseFor(clientWithNameQuery()) shouldEqual(OK, "GET,/prefix")
      }
    }

    describe("puts the header parameters into the request") {
      val returnsHeaders = Service.mk[HttpRequest, HttpResponse] { request => Future.value(Ok(headersFrom(request).toString())) }

      val nameHeader = Header.optional.string("name")

      val clientWithNameHeader = ClientRoute().taking(nameHeader).at(GET) bindTo returnsHeaders

      it("when there are some, includes them") {
        responseFor(clientWithNameHeader(nameHeader --> "bob")) shouldEqual(OK, "Map(name -> bob, X-Fintrospect-Route-Name -> GET.)")
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
        responseFor(client(intParam --> 55)) shouldEqual (HttpResponseStatus.OK, "Map(X-Fintrospect-Route-Name -> GET.svc/{anInt}/fixed)")
      }
    }
  }

  def responseFor(future: Future[HttpResponse]): (HttpResponseStatus, String) = statusAndContentFrom(result(future))

}
