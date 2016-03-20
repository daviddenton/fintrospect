package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.{BadRequest, Ok}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.formats.PlainText.ResponseBuilder.implicits.statusToResponseBuilderConfig
import io.fintrospect.parameters.{Header, Path, Query}
import io.fintrospect.util.HttpRequestResponseUtil.{headersFrom, statusAndContentFrom}
import org.scalatest.{FunSpec, ShouldMatchers}

class RouteSpecTest extends FunSpec with ShouldMatchers {

  describe("Http Route as a client") {
    val returnsMethodAndUri = Service.mk[Request, Response] { request =>
      Ok(request.method.toString() + "," + request.uri)
    }
    val name = Path.string("name")
    val maxAge = Path.integer("maxAge")
    val gender = Path.string("gender")
    val clientWithNoParameters = RouteSpec().at(Get) bindToClient returnsMethodAndUri

    val clientWithNameAndMaxAgeAndGender = RouteSpec().at(Get) / name / maxAge / gender bindToClient returnsMethodAndUri

    describe("invalid parameters are dealt with") {
      it("missing parameters throw up") {
        responseFor(clientWithNameAndMaxAgeAndGender()) shouldEqual(BadRequest, "Client: Missing required params passed: Set({name}, {maxAge}, {gender})")
      }
      it("unknown parameters returns bad request") {
        responseFor(clientWithNoParameters(maxAge.of(7))) shouldEqual(BadRequest, "Client: Unknown params passed: Set({maxAge})")
      }
    }

    describe("converts the path parameters into the correct url") {
      it("when there are none") {
        responseFor(clientWithNoParameters()) shouldEqual(Ok, "GET,/")
      }
      it("when there are some") {
        responseFor(clientWithNameAndMaxAgeAndGender(gender --> "male", maxAge --> 7, name.-->("bob"))) shouldEqual(Ok, "GET,/bob/7/male")
      }
      it("ignores fixed") {
        val clientWithFixedSections = RouteSpec().at(Get) / "prefix" / maxAge / "suffix" bindToClient returnsMethodAndUri
        responseFor(clientWithFixedSections(maxAge --> 7)) shouldEqual(Ok, "GET,/prefix/7/suffix")
      }
    }

    describe("converts the query parameters into the correct url format") {
      val nameQuery = Query.optional.string("name")
      val clientWithNameQuery = RouteSpec().taking(nameQuery).at(Get) / "prefix" bindToClient returnsMethodAndUri

      it("when there are some") {
        responseFor(clientWithNameQuery(nameQuery --> Option("bob"))) shouldEqual(Ok, "GET,/prefix?name=bob")
      }
      it("optional query params are ignored if not there") {
        responseFor(clientWithNameQuery()) shouldEqual(Ok, "GET,/prefix")
      }
    }

    describe("puts the header parameters into the request") {
      val returnsHeaders = Service.mk[Request, Response] { request => Ok(headersFrom(request).toString()) }

      val nameHeader = Header.optional.string("name")

      val clientWithNameHeader = RouteSpec().taking(nameHeader).at(Get) bindToClient returnsHeaders

      it("when there are some, includes them") {
        responseFor(clientWithNameHeader(nameHeader --> "bob")) shouldEqual(Ok, "Map(X-Fintrospect-Route-Name -> GET:, name -> bob)")
      }
      it("optional query params are ignored if not there") {
        responseFor(clientWithNameHeader()) shouldEqual(Ok, "Map(X-Fintrospect-Route-Name -> GET:)")
      }
    }

    describe("identifies") {
      val returnsHeaders = Service.mk[Request, Response] { request => Ok(headersFrom(request).toString()) }

      val intParam = Path.int("anInt")

      val client = RouteSpec().at(Get) / "svc" / intParam / Path.fixed("fixed") bindToClient returnsHeaders

      it("identifies called route as a request header") {
        responseFor(client(intParam --> 55)) shouldEqual(Ok, "Map(X-Fintrospect-Route-Name -> GET:/svc/{anInt}/fixed)")
      }
    }
  }

  def responseFor(future: Future[Response]): (Status, String) = statusAndContentFrom(result(future))

}
