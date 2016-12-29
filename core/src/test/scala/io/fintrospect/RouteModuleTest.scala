package io.fintrospect

import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.formats.Argo
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.formats.ResponseBuilder.responseToFuture
import io.fintrospect.parameters._
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.util.HttpRequestResponseUtil
import io.fintrospect.util.HttpRequestResponseUtil.{headersFrom, statusAndContentFrom}
import org.scalatest.{FunSpec, Matchers}

class RouteModuleTest extends FunSpec with Matchers {

  case class AService(segments: Seq[String]) extends Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      Ok(segments.mkString(","))
    }
  }

  describe("RouteModule") {
    describe("when a route path can be found") {
      val m = RouteModule(Root)
      val d = RouteSpec("")

      it("with 0 segment (convenience)") {
        assertOkResponse(m.withRoute(d.at(Get) / "svc" bindTo AService(Nil)), Nil)
      }

      it("with 0 segment") {
        assertOkResponse(m.withRoute(d.at(Get) / "svc" bindTo AService(Nil)), Nil)
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

      it("with all fixed segments") {
        val module = m.withRoute(d.at(Get) / "svc" / "1" / "2" / "3" / "4" / "5" / "6" bindTo Service.mk { r: Request => Response() })
        Await.result(module.toService(Request("/svc/1/2/3/4/5/6"))).status shouldBe Status.Ok
      }

      it("with trailing segments") {
        val module = m.withRoute(d.at(Get) / "svc" / Path.string("first") / "a" / "b" / "c" / "d" / "e" / "f"
          bindTo ((_1, _2, _3, _4, _5, _6, _7) => AService(Seq(_2, _3, _4, _5, _6, _7))))

        Await.result(module.toService(Request("/svc/1/a/b/c/d/e/f"))).status shouldBe Status.Ok
      }
    }

    describe("description route is added") {
      it("at default location at the root of the module") {
        val m = RouteModule(Root, SimpleJson())
        statusAndContentFrom(result(m.toService(Request("/")))) shouldBe(Status.Ok, SimpleJson().description(Root, NoSecurity, Nil).contentString)
      }

      it("at custom location") {
        val m = RouteModule(Root, SimpleJson()).withDescriptionPath(_ / "bob")
        statusAndContentFrom(result(m.toService(Request("/bob")))) shouldBe(Status.Ok, SimpleJson().description(Root, NoSecurity, Nil).contentString)

        Await.result(m.toService(Request("/"))).status shouldBe Status.NotFound
      }
    }

    describe("when no module renderer is used") {
      it("at default location at the root of the module") {
        val m = RouteModule(Root)
        statusAndContentFrom(result(m.toService(Request("/")))) shouldBe(Status.NotFound, "")
      }
    }

    describe("can combine more than 2 modules") {
      it("can get to all routes") {

        def module(path: String) = {
          RouteModule(Root / path).withRoute(RouteSpec("").at(Get) / "echo" bindTo Service.mk[Request, Response] { _ => Ok(path) })
        }

        val totalService = Module.combine(module("rita"), module("bob"), module("sue")).toService

        statusAndContentFrom(result(totalService(Request("/rita/echo")))) shouldBe(Status.Ok, "rita")
        statusAndContentFrom(result(totalService(Request("/bob/echo")))) shouldBe(Status.Ok, "bob")
        statusAndContentFrom(result(totalService(Request("/sue/echo")))) shouldBe(Status.Ok, "sue")
      }
    }

    describe("when a route path cannot be found") {
      it("returns a 404") {
        result(RouteModule(Root).toService(Request("/svc/noSuchRoute"))).status shouldBe Status.NotFound
      }
    }

    describe("filters") {
      val module = RouteModule[Request, Response](Root, SimpleJson(), Filter.mk((in, svc) => {
        svc(in).flatMap(resp => {
          resp.headerMap.add("MYHEADER", "BOB")
          resp
        })
      }))
        .withRoute(RouteSpec("").at(Get) / "svc" bindTo AService(Nil))

      it("applies to routes in module") {
        result(module.toService(Request("/svc"))).headerMap("MYHEADER") shouldBe "BOB"
      }
      it("does not apply to  headers to all routes in module") {
        result(module.toService(Request("/"))).headerMap.contains("MYHEADER") shouldBe false
      }
    }

    describe("when a valid path does not contain all required request parameters") {
      val d = RouteSpec("").taking(Header.required.int("aNumberHeader"))
      val m = RouteModule(Root).withRoute(d.at(Get) / "svc" bindTo AService(Nil))

      it("it returns a 400 when the required param is missing") {
        val request = Request("/svc")
        result(m.toService(request)).status shouldBe Status.BadRequest
      }

      it("it returns a 400 when the required param is not the correct type") {
        val request = Request("/svc")
        request.headerMap.add("aNumberHeader", "notANumber")
        result(m.toService(request)).status shouldBe Status.BadRequest
      }
    }

    describe("when a valid path does not contain all required form fields") {
      val d = RouteSpec("").body(Body.form(FormField.required.int("aNumber")))
      val service = RouteModule(Root).withRoute(d.at(Post) / "svc" bindTo AService(Nil)).toService

      it("it returns a 400 when a required form field is missing") {
        val request = Request(Post, "/svc")
        result(service(request)).status shouldBe Status.BadRequest
      }

      it("it returns a 400 when the required form field is not the correct type") {
        val request = Request(Post, "/svc")
        request.setContentString("aNumber=notANumber")
        result(service(request)).status shouldBe Status.BadRequest
      }
    }

    describe("when a valid path does not contain required JSON body") {
      val d = RouteSpec("").body(Body.json(None, Argo.JsonFormat.obj()))
      val service = RouteModule(Root).withRoute(d.at(Get) / "svc" bindTo AService(Nil)).toService

      it("it returns a 400 when the required body is missing") {
        val request = Request("/svc")
        result(service(request)).status shouldBe Status.BadRequest
      }

      it("it returns a 400 when the required body is not the correct type") {
        val request = Request("/svc")
        request.setContentString("notAJsonBlob")
        result(service(request)).status shouldBe Status.BadRequest
      }
    }

    describe("when a valid path does not contain required custom body") {
      val body = Body[Int](BodySpec.string(None, ContentTypes.TEXT_PLAIN).map(_.toInt, _.toString), example = 1)
      val d = RouteSpec("").body(body)
      val service = RouteModule(Root).withRoute(d.at(Get) / "svc" bindTo AService(Nil)).toService

      it("it returns a 400 when the required body is missing") {
        val request = Request("/svc")
        result(service(request)).status shouldBe Status.BadRequest
      }

      it("it returns a 400 when the required body is not the correct type") {
        val request = Request("/svc")
        request.setContentString("notAnInt")
        result(service(request)).status shouldBe Status.BadRequest
      }
    }

    describe("when a valid path contains illegal values for an optional parameter") {
      val d = RouteSpec("").taking(Header.optional.int("aNumberHeader"))
      val service = RouteModule(Root).withRoute(d.at(Get) / "svc" bindTo AService(Nil)).toService

      it("it returns a 200 when the optional param is missing") {
        val request = Request("/svc")
        result(service(request)).status shouldBe Status.Ok
      }

      it("it returns a 400 when the optional param is not the correct type") {
        val request = Request("/svc")
        request.headerMap.add("aNumberHeader", "notANumber")
        result(service(request)).status shouldBe Status.BadRequest
      }
    }

    describe("identity") {
      it("identifies route with anonymised description when called") {
        def getHeaders(number: Int, aString: String) = Service.mk[Request, Response] { request => Ok(headersFrom(request).toString()) }

        val route = RouteSpec("").at(Get) / "svc" / Path.int("anInt") / Path.fixed("fixed") bindTo getHeaders
        val m = RouteModule(Root).withRoute(route)
        HttpRequestResponseUtil.statusAndContentFrom(result(m.toService(Request("svc/1/fixed")))) shouldBe(Status.Ok, "Map(X-Fintrospect-Route-Name -> GET:/svc/{anInt}/fixed)")
      }
    }

  }

  def assertOkResponse(module: RouteModule[_, _], segments: Seq[String]): Unit = {
    val result = Await.result(module.toService(Request("/svc/" + segments.mkString("/"))))
    result.status shouldBe Status.Ok
    result.contentString shouldBe segments.mkString(",")
  }

}
