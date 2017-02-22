package io.fintrospect

import java.time.LocalDate

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.path.{Root, Path => FPath}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.formats.Argo.JsonFormat.obj
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.parameters._
import io.fintrospect.util.ExtractionError.Missing
import io.fintrospect.util.HttpRequestResponseUtil.{headersFrom, statusAndContentFrom}
import io.fintrospect.util.{Extracted, ExtractionFailed}
import org.scalatest.{FunSpec, Matchers}

class RouteSpecTest extends FunSpec with Matchers {

  describe("Http Route as a client") {
    val returnsMethodAndUri = Service.mk[Request, Response] { request =>
      Ok(request.method.toString() + "," + request.uri)
    }
    val returnsBody = Service.mk[Request, Response] { request =>
      Ok(request.contentString)
    }
    val query = Query.required.string("query")
    val name = Path.string("pathName")
    val maxAge = Path.integer("maxAge")
    val gender = Path.string("gender")
    val clientWithNoParameters = RouteSpec().at(Get) bindToClient returnsMethodAndUri

    val clientWithQueryNameAndMaxAgeAndGender = RouteSpec().taking(query).at(Get) / name / maxAge / gender bindToClient returnsMethodAndUri

    describe("invalid parameters are dealt with") {
      it("missing request parameters throws up") {
        intercept[BrokenContract](result(clientWithQueryNameAndMaxAgeAndGender(name --> "bob", maxAge --> 7, gender --> "male"))).getMessage shouldBe "Client: Missing required params passed: [Mandatory parameter query (string) in query]"
      }
      it("missing path parameters throws up") {
        intercept[BrokenContract](result(clientWithQueryNameAndMaxAgeAndGender(query --> "bob", maxAge --> 7, gender --> "male"))).getMessage shouldBe "Client: Missing required params passed: [{pathName}]"
      }
      it("unknown parameters throws up") {
        intercept[BrokenContract](result(clientWithNoParameters(maxAge.of(7)))).getMessage shouldBe "Client: Unknown params passed: [{maxAge}]"
      }
    }

    describe("converts the path parameters into the correct url") {
      it("when there are none") {
        responseFor(clientWithNoParameters()) shouldBe(Status.Ok, "GET,/")
      }
      it("when there are some") {
        responseFor(clientWithQueryNameAndMaxAgeAndGender(query --> "queryValue", gender --> "male", maxAge --> 7, name.-->("bob"))) shouldBe(Status.Ok, "GET,/bob/7/male?query=queryValue")
      }
      it("ignores fixed") {
        val clientWithFixedSections = RouteSpec().at(Get) / "prefix" / maxAge / "suffix" bindToClient returnsMethodAndUri
        responseFor(clientWithFixedSections(maxAge --> 7)) shouldBe(Status.Ok, "GET,/prefix/7/suffix")
      }
    }

    describe("populates the body into the request") {
      it("form") {
        val field = FormField.required.string("bob")
        val form = Body.form(field)
        val client = RouteSpec().body(form).at(Get) bindToClient returnsBody
        val response = result(client(form --> Form(field --> "value")))
        response.status shouldBe Status.Ok
        response.contentString shouldBe "bob=value"
      }
    }

    describe("converts the query parameters into the correct url format") {
      val nameQuery = Query.optional.string("name")
      val clientWithNameQuery = RouteSpec().taking(nameQuery).at(Get) / "prefix" bindToClient returnsMethodAndUri

      it("when there are some") {
        responseFor(clientWithNameQuery(nameQuery --> Option("bob"))) shouldBe(Status.Ok, "GET,/prefix?name=bob")
      }
      it("optional query params are ignored if not there") {
        responseFor(clientWithNameQuery()) shouldBe(Status.Ok, "GET,/prefix")
      }
    }

    describe("puts the header parameters into the request") {
      val returnsHeaders = Service.mk[Request, Response] { request => Ok(headersFrom(request).toString()) }

      val nameHeader = Header.optional.string("name")

      val clientWithNameHeader = RouteSpec().taking(nameHeader).at(Get) bindToClient returnsHeaders

      it("when there are some, includes them") {
        responseFor(clientWithNameHeader(nameHeader --> "bob")) shouldBe(Status.Ok, "Map(X-Fintrospect-Route-Name -> GET:, name -> bob)")
      }
      it("optional query params are ignored if not there") {
        responseFor(clientWithNameHeader()) shouldBe(Status.Ok, "Map(X-Fintrospect-Route-Name -> GET:)")
      }
    }

    describe("identifies") {
      val returnsHeaders = Service.mk[Request, Response] { request => Ok(headersFrom(request).toString()) }

      val intParam = Path.int("anInt")

      val client = RouteSpec().at(Get) / "svc" / intParam / Path.fixed("fixed") bindToClient returnsHeaders

      it("identifies called route as a request header") {
        responseFor(client(intParam --> 55)) shouldBe(Status.Ok, "Map(X-Fintrospect-Route-Name -> GET:/svc/{anInt}/fixed)")
      }
    }
  }

  describe("Http Route as a proxy") {
    val request = Request()
    request.contentString = <xml/>.toString()
    request.headerMap("date") = "2000-01-01"

    val query = Query.required.string("query")
    val date = Header.optional.localDate("date")
    val gender = Path.string("gender")
    val body = Body.xml()

    val expectedRequest = Service.mk[Request, Response] { received =>
      if (query.from(received) == "bob" &&
        date.from(received).contains(LocalDate.of(2000, 1, 1)) &&
        request.uri == received.uri &&
        body.from(received) == <xml/>
      ) HttpResponse(Status.Ok)
      else HttpResponse(Status.BadRequest)
    }

    def checkProxyRoute(urlParts: String, fn: (UnboundRoute0 => UnboundRoute)): Unit = {
      request.uri = urlParts + "?query=bob"
      val route = fn(RouteSpec().taking(query).taking(date).body(body).at(Get))

      val proxyService = RouteModule(Root).withRoute(route bindToProxy expectedRequest).toService
      result(proxyService(request)).status shouldBe Status.Ok
    }

    it("copies everything into downstream request") {
      checkProxyRoute("/", identity)
      checkProxyRoute("/male", _ / gender)
      checkProxyRoute("/male/1", _ / gender / "1")
      checkProxyRoute("/male/1/2", _ / gender / "1" / "2")
      checkProxyRoute("/male/1/2/3", _ / gender / "1" / "2" / "3")
      checkProxyRoute("/male/1/2/3/4", _ / gender / "1" / "2" / "3" / "4")
      checkProxyRoute("/male/1/2/3/4/5", _ / gender / "1" / "2" / "3" / "4" / "5")
      checkProxyRoute("/male/1/2/3/4/5/6", _ / gender / "1" / "2" / "3" / "4" / "5" / "6")
    }
  }

  describe("create reverse router") {

    it("0 elements") {
      RouteSpec().at(Get).reverseRouter(Root / "prefix")().toString shouldBe "/prefix/"
    }

    it("prefix only") {
      val router = RouteSpec().at(Get) / "hello" reverseRouter (Root / "prefix")
      router().toString shouldBe "/prefix/hello/"
    }

    it("1 element") {
      val router = RouteSpec().at(Get) / "hello" / Path.int("first") reverseRouter (Root / "prefix")
      router(1).toString shouldBe "/prefix/hello/1"
    }

    it("2 elements") {
      val router = RouteSpec().at(Get) / "hello" / Path.int("first") / Path.boolean("first") reverseRouter (Root / "prefix")
      router(1, false).toString shouldBe "/prefix/hello/1/false"
    }

    it("3 elements") {
      val router = RouteSpec().at(Get) / "hello" / Path.int("first") / Path.boolean("first") / Path.boolean("second") reverseRouter (Root / "prefix")
      router(1, false, true).toString shouldBe "/prefix/hello/1/false/true"
    }
  }


  describe("retrieval from a pre-extracted request") {
    val param = Query.required.string("bob")
    val body = Body.json()

    val spec = RouteSpec().taking(param).body(body)

    it("succeeds when nothing missing") {
      val request = Request("?bob=bill")
      request.contentString = "{}"
      spec <--? request shouldBe Extracted(ExtractedRouteRequest(request, Map(param -> Extracted("bill"), body -> Extracted(obj()))))
    }

    it("fails on missing param") {
      val request = Request()
      request.contentString = "{}"
      spec <--? request shouldBe ExtractionFailed(Missing(param))
    }

    it("fails on missing body") {
      val request = Request("?bob=bill")
      spec <--? request match {
        case ExtractionFailed(ps) => ps.head.param.name shouldBe "body"
        case _ => fail("did not fail to extract")
      }
    }
  }

  def responseFor(future: Future[Response]): (Status, String) = statusAndContentFrom(result(future))

}
