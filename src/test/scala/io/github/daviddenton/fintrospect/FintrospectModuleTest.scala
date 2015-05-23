package io.github.daviddenton.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request => FRq}
import com.twitter.io.Charsets._
import com.twitter.util.{Await, Future}
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect.parameters.Header
import io.github.daviddenton.fintrospect.parameters.Path._
import io.github.daviddenton.fintrospect.renderers.SimpleJson
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse, HttpResponseStatus}
import org.scalatest.{FunSpec, ShouldMatchers}

class FintrospectModuleTest extends FunSpec with ShouldMatchers {

  case class AService(segments: Seq[String]) extends Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest): Future[HttpResponse] = {
      Json.Ok(segments.mkString(","))
    }
  }

  describe("FintrospectModule") {
    describe("when a route path can be found") {
      val m = FintrospectModule(Root, SimpleJson())
      val d = DescribedRoute("")

      it("with 0 segment") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" bindTo (() => AService(Seq()))), Seq())
      }
      it("with 1 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" / string("s1") bindTo ((_1: String) => AService(Seq(_1)))), Seq("a"))
      }
      it("with 2 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" / string("s1") / string("s2") bindTo ((_1: String, _2: String) => AService(Seq(_1, _2)))), Seq("a", "b"))
      }
      it("with 3 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" / string("s1") / string("s2") / string("s3") bindTo ((_1: String, _2: String, _3: String) => AService(Seq(_1, _2, _3)))), Seq("a", "b", "c"))
      }
      it("with 4 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" / string("s1") / string("s2") / string("s3") / string("s4") bindTo ((_1: String, _2: String, _3: String, _4: String) => AService(Seq(_1, _2, _3, _4)))), Seq("a", "b", "c", "d"))
      }
      it("with 5 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" / string("s1") / string("s2") / string("s3") / string("s4") / string("s5") bindTo ((_1: String, _2: String, _3: String, _4: String, _5: String) => AService(Seq(_1, _2, _3, _4, _5)))), Seq("a", "b",
          "c", "d", "e"))
      }
    }

    describe("can combine more than 2 modules") {
      it("can get to all routes") {
        def module(path: String) = {
          FintrospectModule(Root / path, SimpleJson()).withRoute(DescribedRoute("").at(GET) bindTo (() => new Service[HttpRequest, HttpResponse] {
            def apply(request: HttpRequest): Future[HttpResponse] = Json.Ok(path)
          }))
        }
        val totalService = FintrospectModule.toService(combine(module("rita"), module("bob"), module("sue")))

        Await.result(totalService.apply(FRq("/rita"))).getContent.toString(Utf8) shouldEqual "rita"
        Await.result(totalService.apply(FRq("/bob"))).getContent.toString(Utf8) shouldEqual "bob"
        Await.result(totalService.apply(FRq("/sue"))).getContent.toString(Utf8) shouldEqual "sue"
      }
    }

    describe("when a route path cannot be found") {
      it("returns a 404") {
        val result = Await.result(FintrospectModule(Root, SimpleJson()).toService.apply(FRq("/svc/noSuchRoute")))
        result.getStatus shouldEqual HttpResponseStatus.NOT_FOUND
      }
    }

    describe("when a valid path does not contain all required parameters") {
      val d = DescribedRoute("").taking(Header.required.int("aNumberHeader"))
      val m = FintrospectModule(Root, SimpleJson()).withRoute(d.at(GET) / "svc" bindTo (() => AService(Seq())))

      it("it returns a 400 when the required param is missing") {
        val request = FRq("/svc")
        Await.result(m.toService.apply(request)).getStatus shouldEqual HttpResponseStatus.BAD_REQUEST
      }

      it("it returns a 400 when the required param is not the correct type") {
        val request = FRq("/svc")
        request.headers().add("aNumberHeader", "notANumber")
        Await.result(m.toService.apply(request)).getStatus shouldEqual HttpResponseStatus.BAD_REQUEST
      }
    }

    describe("when a valid path contains illegal values for an optional parameter") {
      val d = DescribedRoute("").taking(Header.optional.int("aNumberHeader"))
      val m = FintrospectModule(Root, SimpleJson()).withRoute(d.at(GET) / "svc" bindTo (() => AService(Seq())))

      it("it returns a 200 when the optional param is missing") {
        val request = FRq("/svc")
        Await.result(m.toService.apply(request)).getStatus shouldEqual HttpResponseStatus.OK
      }

      it("it returns a 400 when the optional param is not the correct type") {
        val request = FRq("/svc")
        request.headers().add("aNumberHeader", "notANumber")
        Await.result(m.toService.apply(request)).getStatus shouldEqual HttpResponseStatus.BAD_REQUEST
      }
    }
  }

  def assertOkResponse(module: FintrospectModule, segments: Seq[String]): Unit = {
    val result = Await.result(module.toService.apply(FRq("/svc/" + segments.mkString("/"))))
    result.getStatus shouldEqual HttpResponseStatus.OK
    result.getContent.toString(Utf8) shouldEqual segments.mkString(",")
  }
}
