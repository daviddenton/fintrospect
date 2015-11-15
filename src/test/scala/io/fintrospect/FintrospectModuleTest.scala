package io.fintrospect

import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Await._
import com.twitter.util.{Await, Future}
import io.fintrospect.FintrospectModule._
import io.fintrospect.formats.ResponseBuilder.toFuture
import io.fintrospect.formats.json.Argo
import io.fintrospect.formats.text.PlainTextResponseBuilder._
import io.fintrospect.parameters._
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.util.HttpRequestResponseUtil
import io.fintrospect.util.HttpRequestResponseUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

class FintrospectModuleTest extends FunSpec with ShouldMatchers {

  case class AService(segments: Seq[String]) extends Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      Ok(segments.mkString(","))
    }
  }

  describe("FintrospectModule") {
    describe("when a route path can be found") {
      val m = FintrospectModule(Root, SimpleJson())
      val d = RouteSpec("")

      it("with 0 segment") {
        assertOkResponse(m.withRoute(d.at(Get) / "svc" bindTo (() => AService(Nil))), Nil)
      }
      it("with 1 segment") {
        assertOkResponse(m.withRoute(d.at(Get) / Path.fixed("svc") bindTo ((_1) => AService(Nil))), Nil)
      }
      it("with 2 segments") {
        assertOkResponse(m.withRoute(d.at(Get) / Path.fixed("svc") / Path.string("s1")
          bindTo ((_1, _2: String) => AService(Seq(_2)))), Seq("a"))
      }
      it("with 3 segments") {
        assertOkResponse(m.withRoute(d.at(Get) / Path.fixed("svc") / Path.string("s1") / Path.string("s2")
          bindTo ((_1, _2, _3) => AService(Seq(_2, _3)))), Seq("a", "b"))
      }
      it("with 4 segments") {
        assertOkResponse(m.withRoute(d.at(Get) / Path.fixed("svc") / Path.string("s1") / Path.string("s2") / Path.string("s3")
          bindTo ((_1, _2, _3, _4) => AService(Seq(_2, _3, _4)))), Seq("a", "b", "c"))
      }
      it("with 5 segments") {
        assertOkResponse(m.withRoute(d.at(Get) / Path.fixed("svc") / Path.string("s1") / Path.string("s2") / Path.string("s3") / Path.string("s4")
          bindTo ((_1, _2, _3, _4, _5) => AService(Seq(_2, _3, _4, _5)))), Seq("a", "b", "c", "d"))
      }
      it("with 6 segments") {
        assertOkResponse(m.withRoute(d.at(Get) / Path.fixed("svc") / Path.string("s1") / Path.string("s2") / Path.string("s3") / Path.string("s4") / Path.string("s5")
          bindTo ((_1, _2, _3, _4, _5, _6) => AService(Seq(_2, _3, _4, _5, _6)))), Seq("a", "b", "c", "d", "e"))
      }
      it("with 7 segments") {
        assertOkResponse(m.withRoute(d.at(Get) / Path.fixed("svc") / Path.string("s1") / Path.string("s2") / Path.string("s3") / Path.string("s4") / Path.string("s5") / Path.string("s6")
          bindTo ((_1, _2, _3, _4, _5, _6, _7) => AService(Seq(_2, _3, _4, _5, _6, _7)))), Seq("a", "b", "c", "d", "e", "f"))
      }
    }

    describe("description route is added") {

      it("at default location at the root of the module") {
        val m = FintrospectModule(Root, SimpleJson())
        statusAndContentFrom(result(m.toService(Request("/")))) shouldEqual(Status.Ok, contentFrom(SimpleJson().description(Root, Nil)))
      }

      it("at custom location") {
        val m = FintrospectModule(Root, SimpleJson()).withDescriptionPath(_ / "bob")
        statusAndContentFrom(result(m.toService(Request("/bob")))) shouldEqual(Status.Ok, contentFrom(SimpleJson().description(Root, Nil)))

        Await.result(m.toService(Request("/"))).status shouldEqual Status.NotFound
      }
    }

    describe("can combine more than 2 modules") {
      it("can get to all routes") {
        def module(path: String) = {
          FintrospectModule(Root / path, SimpleJson()).withRoutes(Seq(RouteSpec("").at(Get) / "echo" bindTo (() => new Service[Request, Response] {
            def apply(request: Request): Future[Response] = Ok(path)
          })))
        }
        val totalService = FintrospectModule.toService(combine(module("rita"), module("bob"), module("sue")))

        statusAndContentFrom(result(totalService(Request("/rita/echo")))) shouldEqual(Status.Ok, "rita")
        statusAndContentFrom(result(totalService(Request("/bob/echo")))) shouldEqual(Status.Ok, "bob")
        statusAndContentFrom(result(totalService(Request("/sue/echo")))) shouldEqual(Status.Ok, "sue")
      }
    }

    describe("when a route path cannot be found") {
      it("returns a 404") {
        result(FintrospectModule(Root, SimpleJson()).toService(Request("/svc/noSuchRoute"))).status shouldEqual Status.NotFound
      }
    }

    describe("filters") {
      val module = FintrospectModule(Root, SimpleJson(), Filter.mk((in, svc) => {
        svc(in).flatMap(resp => {
          resp.headerMap.add("MYHEADER", "BOB")
          resp
        })
      }))
        .withRoute(RouteSpec("").at(Get) / "svc" bindTo (() => AService(Nil)))

      it("applies to routes in module") {
        result(module.toService(Request("/svc"))).headerMap("MYHEADER") shouldEqual "BOB"
      }
      it("does not apply to  headers to all routes in module") {
        result(module.toService(Request("/"))).headerMap.contains("MYHEADER") shouldEqual false
      }
    }

    describe("when a valid path does not contain all required request parameters") {
      val d = RouteSpec("").taking(Header.required.int("aNumberHeader"))
      val m = FintrospectModule(Root, SimpleJson()).withRoute(d.at(Get) / "svc" bindTo (() => AService(Nil)))

      it("it returns a 400 when the required param is missing") {
        val request = Request("/svc")
        result(m.toService(request)).status shouldEqual Status.BadRequest
      }

      it("it returns a 400 when the required param is not the correct type") {
        val request = Request("/svc")
        request.headerMap.add("aNumberHeader", "notANumber")
        result(m.toService(request)).status shouldEqual Status.BadRequest
      }
    }

    describe("when a valid path does not contain all required form fields") {
      val d = RouteSpec("").body(Body.form(FormField.required.int("aNumber")))
      val service = FintrospectModule(Root, SimpleJson()).withRoute(d.at(Post) / "svc" bindTo (() => AService(Nil))).toService

      it("it returns a 400 when a required form field is missing") {
        val request = Request(Post, "/svc")
        result(service(request)).status shouldEqual Status.BadRequest
      }

      it("it returns a 400 when the required form field is not the correct type") {
        val request = Request(Post, "/svc")
        request.setContentString("aNumber=notANumber")
        result(service(request)).status shouldEqual Status.BadRequest
      }
    }

    describe("when a valid path does not contain required JSON body") {
      val d = RouteSpec("").body(Body.json(None, Argo.JsonFormat.obj()))
      val service = FintrospectModule(Root, SimpleJson()).withRoute(d.at(Get) / "svc" bindTo (() => AService(Nil))).toService

      it("it returns a 400 when the required body is missing") {
        val request = Request("/svc")
        result(service(request)).status shouldEqual Status.BadRequest
      }

      it("it returns a 400 when the required body is not the correct type") {
        val request = Request("/svc")
        request.setContentString("notAJsonBlob")
        result(service(request)).status shouldEqual Status.BadRequest
      }
    }

    describe("when a valid path does not contain required custom body") {
      val body = Body[Int](BodySpec[Int](None, ContentTypes.TEXT_PLAIN, _.toInt, _.toString), example = 1)
      val d = RouteSpec("").body(body)
      val service = FintrospectModule(Root, SimpleJson()).withRoute(d.at(Get) / "svc" bindTo (() => AService(Nil))).toService

      it("it returns a 400 when the required body is missing") {
        val request = Request("/svc")
        result(service(request)).status shouldEqual Status.BadRequest
      }

      it("it returns a 400 when the required body is not the correct type") {
        val request = Request("/svc")
        request.setContentString("notAnInt")
        result(service(request)).status shouldEqual Status.BadRequest
      }
    }

    describe("when a valid path contains illegal values for an optional parameter") {
      val d = RouteSpec("").taking(Header.optional.int("aNumberHeader"))
      val service = FintrospectModule(Root, SimpleJson()).withRoute(d.at(Get) / "svc" bindTo (() => AService(Nil))).toService

      it("it returns a 200 when the optional param is missing") {
        val request = Request("/svc")
        result(service(request)).status shouldEqual Status.Ok
      }

      it("it returns a 400 when the optional param is not the correct type") {
        val request = Request("/svc")
        request.headerMap.add("aNumberHeader", "notANumber")
        result(service(request)).status shouldEqual Status.BadRequest
      }
    }

    describe("identity") {
      it("identifies route with anonymised description when called") {
        def getHeaders(number: Int, aString: String) = Service.mk[Request, Response] { request => Ok(headersFrom(request).toString()) }
        val route = RouteSpec("").at(Get) / "svc" / Path.int("anInt") / Path.fixed("fixed") bindTo getHeaders
        val m = FintrospectModule(Root, SimpleJson()).withRoute(route)
        HttpRequestResponseUtil.statusAndContentFrom(result(m.toService(Request("svc/1/fixed")))) shouldEqual(Status.Ok, "Map(X-Fintrospect-Route-Name -> GET:/svc/{anInt}/fixed)")
      }
    }

  }

  def assertOkResponse(module: FintrospectModule, segments: Seq[String]): Unit = {
    val result = Await.result(module.toService(Request("/svc/" + segments.mkString("/"))))
    result.status shouldEqual Status.Ok
    result.contentString shouldEqual segments.mkString(",")
  }

}
