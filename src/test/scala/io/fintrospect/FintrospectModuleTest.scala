package io.fintrospect

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Charsets._
import com.twitter.util.Await._
import com.twitter.util.{Await, Future}
import io.fintrospect.FintrospectModule._
import io.fintrospect.parameters._
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.util.HttpRequestResponseUtil._
import io.fintrospect.util.PlainTextResponseBuilder._
import io.fintrospect.util.ResponseBuilder._
import io.fintrospect.util.{ArgoUtil, HttpRequestResponseUtil}
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}
import org.scalatest.{FunSpec, ShouldMatchers}

class FintrospectModuleTest extends FunSpec with ShouldMatchers {

  case class AService(segments: Seq[String]) extends Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest): Future[HttpResponse] = {
      Ok(segments.mkString(","))
    }
  }

  describe("FintrospectModule") {
    describe("when a route path can be found") {
      val m = FintrospectModule(Root, SimpleJson())
      val d = RouteSpec("")

      it("with 0 segment") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" bindTo (() => AService(Seq()))), Seq())
      }
      it("with 1 segment") {
        assertOkResponse(m.withRoute(d.at(GET) / Path.fixed("svc") bindTo ((_1) => AService(Seq()))), Seq())
      }
      it("with 2 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / Path.fixed("svc") / Path.string("s1") bindTo ((_1, _2:String) => AService(Seq(_2)))), Seq("a"))
      }
      it("with 3 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / Path.fixed("svc") / Path.string("s1") / Path.string("s2") bindTo ((_1, _2, _3) => AService(Seq(_2, _3)))), Seq("a", "b"))
      }
      it("with 4 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / Path.fixed("svc") / Path.string("s1") / Path.string("s2") / Path.string("s3") bindTo ((_1, _2, _3, _4) => AService(Seq(_2, _3, _4)))), Seq("a", "b", "c"))
      }
      it("with 5 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / Path.fixed("svc") / Path.string("s1") / Path.string("s2") / Path.string("s3") / Path.string("s4") bindTo ((_1, _2, _3, _4, _5) => AService(Seq(_2, _3, _4, _5)))), Seq
          ("a", "b", "c", "d"))
      }
    }

    describe("description route is added") {

      it("at default location at the root of the module") {
        val m = FintrospectModule(Root, SimpleJson())
        statusAndContentFrom(result(m.toService(Request("/")))) shouldEqual(OK, contentFrom(SimpleJson().description(Root, Seq())))
      }

      it("at custom location") {
        val m = FintrospectModule(Root, SimpleJson()).withDescriptionPath(_ / "bob")
        statusAndContentFrom(result(m.toService(Request("/bob")))) shouldEqual(OK, contentFrom(SimpleJson().description(Root, Seq())))

        Await.result(m.toService(Request("/"))).getStatus shouldEqual NOT_FOUND
      }
    }

    describe("can combine more than 2 modules") {
      it("can get to all routes") {
        def module(path: String) = {
          FintrospectModule(Root / path, SimpleJson()).withRoute(RouteSpec("").at(GET) / "echo" bindTo (() => new Service[HttpRequest, HttpResponse] {
            def apply(request: HttpRequest): Future[HttpResponse] = Ok(path)
          }))
        }
        val totalService = FintrospectModule.toService(combine(module("rita"), module("bob"), module("sue")))

        statusAndContentFrom(result(totalService(Request("/rita/echo")))) shouldEqual(OK, "rita")
        statusAndContentFrom(result(totalService(Request("/bob/echo")))) shouldEqual(OK, "bob")
        statusAndContentFrom(result(totalService(Request("/sue/echo")))) shouldEqual(OK, "sue")
      }
    }

    describe("when a route path cannot be found") {
      it("returns a 404") {
        result(FintrospectModule(Root, SimpleJson()).toService(Request("/svc/noSuchRoute"))).getStatus shouldEqual NOT_FOUND
      }
    }

    describe("filters") {
      val module = FintrospectModule(Root, SimpleJson(), Filter.mk((in, svc) => {
        svc(in).flatMap(resp => {
          resp.headers().add("MYHEADER", "BOB")
          resp
        })
      }))
        .withRoute(RouteSpec("").at(GET) / "svc" bindTo (() => AService(Seq())))

      it("applies to routes in module") {
        result(module.toService(Request("/svc"))).headers().get("MYHEADER") shouldEqual "BOB"
      }
      it("does not apply to  headers to all routes in module") {
        result(module.toService(Request("/"))).headers().contains("MYHEADER") shouldEqual false
      }
    }

    describe("when a valid path does not contain all required request parameters") {
      val d = RouteSpec("").taking(Header.required.int("aNumberHeader"))
      val m = FintrospectModule(Root, SimpleJson()).withRoute(d.at(GET) / "svc" bindTo (() => AService(Seq())))

      it("it returns a 400 when the required param is missing") {
        val request = Request("/svc")
        result(m.toService(request)).getStatus shouldEqual BAD_REQUEST
      }

      it("it returns a 400 when the required param is not the correct type") {
        val request = Request("/svc")
        request.headers().add("aNumberHeader", "notANumber")
        result(m.toService(request)).getStatus shouldEqual BAD_REQUEST
      }
    }

    describe("when a valid path does not contain all required form fields") {
      val d = RouteSpec("").body(Body.form(FormField.required.int("aNumber")))
      val service = FintrospectModule(Root, SimpleJson()).withRoute(d.at(POST) / "svc" bindTo (() => AService(Seq()))).toService

      it("it returns a 400 when a required form field is missing") {
        val request = Request(HttpMethod.POST, "/svc")
        result(service(request)).getStatus shouldEqual BAD_REQUEST
      }

      it("it returns a 400 when the required form field is not the correct type") {
        val request = Request(HttpMethod.POST, "/svc")
        request.setContentString("aNumber=notANumber")
        result(service(request)).getStatus shouldEqual BAD_REQUEST
      }
    }

    describe("when a valid path does not contain required JSON body") {
      val d = RouteSpec("").body(Body.json(None, ArgoUtil.obj()))
      val service = FintrospectModule(Root, SimpleJson()).withRoute(d.at(GET) / "svc" bindTo (() => AService(Seq()))).toService

      it("it returns a 400 when the required body is missing") {
        val request = Request("/svc")
        result(service(request)).getStatus shouldEqual BAD_REQUEST
      }

      it("it returns a 400 when the required body is not the correct type") {
        val request = Request("/svc")
        request.setContentString("notAJsonBlob")
        result(service(request)).getStatus shouldEqual BAD_REQUEST
      }
    }

    describe("when a valid path does not contain required custom body") {
      val body: UniBody[Int] = Body[Int](BodySpec[Int](None, ContentTypes.TEXT_PLAIN, _.toInt, _.toString), example = 1)
      val d = RouteSpec("").body(body)
      val service = FintrospectModule(Root, SimpleJson()).withRoute(d.at(GET) / "svc" bindTo (() => AService(Seq()))).toService

      it("it returns a 400 when the required body is missing") {
        val request = Request("/svc")
        result(service(request)).getStatus shouldEqual BAD_REQUEST
      }

      it("it returns a 400 when the required body is not the correct type") {
        val request = Request("/svc")
        request.setContentString("notAnInt")
        result(service(request)).getStatus shouldEqual BAD_REQUEST
      }
    }

    describe("when a valid path contains illegal values for an optional parameter") {
      val d = RouteSpec("").taking(Header.optional.int("aNumberHeader"))
      val service = FintrospectModule(Root, SimpleJson()).withRoute(d.at(GET) / "svc" bindTo (() => AService(Seq()))).toService

      it("it returns a 200 when the optional param is missing") {
        val request = Request("/svc")
        result(service(request)).getStatus shouldEqual OK
      }

      it("it returns a 400 when the optional param is not the correct type") {
        val request = Request("/svc")
        request.headers().add("aNumberHeader", "notANumber")
        result(service(request)).getStatus shouldEqual BAD_REQUEST
      }
    }

    describe("identity") {
      it("identifies route with anonymised description when called") {
        def getHeaders(number: Int, aString: String) = Service.mk[HttpRequest, HttpResponse] { request => Future.value(Ok(headersFrom(request).toString())) }
        val route = RouteSpec("").at(GET) / "svc" / Path.int("anInt") / Path.fixed("fixed") bindTo getHeaders
        val m = FintrospectModule(Root, SimpleJson()).withRoute(route)
        HttpRequestResponseUtil.statusAndContentFrom(result(m.toService(Request("svc/1/fixed")))) shouldEqual(OK, "Map(X-Fintrospect-Route-Name -> GET./svc/{anInt}/fixed)")
      }
    }

  }

  def assertOkResponse(module: FintrospectModule, segments: Seq[String]): Unit = {
    val result = Await.result(module.toService(Request("/svc/" + segments.mkString("/"))))
    result.getStatus shouldEqual OK
    result.getContent.toString(Utf8) shouldEqual segments.mkString(",")
  }

}
