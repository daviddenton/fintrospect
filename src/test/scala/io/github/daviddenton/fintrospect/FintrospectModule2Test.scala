package io.github.daviddenton.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.io.Charsets._
import com.twitter.util.{Await, Future}
import io.github.daviddenton.fintrospect.parameters.Header
import io.github.daviddenton.fintrospect.parameters.Path._
import io.github.daviddenton.fintrospect.renderers.SimpleJson
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.scalatest.{FunSpec, ShouldMatchers}

class FintrospectModule2Test extends FunSpec with ShouldMatchers {

  case class AService(segments: Seq[String]) extends Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val response = Response()
      response.setStatusCode(200)
      response.setContent(copiedBuffer(segments.mkString(","), Utf8))
      Future.value(response)
    }
  }

  describe("FintrospectModule") {
    describe("when a route path can be found") {
      val m = FintrospectModule2(Root, SimpleJson())
      val d = Description("")

      it("with 0 segment") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" then (() => AService(Seq()))), Seq())
      }
      it("with 1 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" / string("s1") then ((_1: String) => AService(Seq(_1)))), Seq("a"))
      }
    }

    describe("when a route path cannot be found") {
      it("returns a 404") {
        val result = Await.result(FintrospectModule2(Root, SimpleJson()).toService.apply(Request("/svc/noSuchRoute")))
        result.status.getCode should be === 404
      }
    }

    describe("Routes valid requests by path") {
      val m = FintrospectModule2(Root, SimpleJson())
      val d = Description("")

      it("with 0 segment") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" then (() => AService(Seq()))), Seq())
      }
      it("with 1 segments") {
        assertOkResponse(m.withRoute(d.at(GET) / "svc" / string("s1") then ((_1: String) => AService(Seq(_1)))), Seq("a"))
      }
    }

    describe("when a valid path does not contain all required parameters") {
      val d = Description("").taking(Header.required.int("aNumberHeader"))
      val m = FintrospectModule2(Root, SimpleJson()).withRoute(d.at(GET) / "svc" then (() => AService(Seq())))

      it("it returns a 400 when the param is missing") {
        val request = Request("/svc")
        val result = Await.result(m.toService.apply(request))
        result.status.getCode should be === 400
      }

      it("it returns a 400 when the param is not the correct type") {
        val request = Request("/svc")
        request.headers().add("aNumberHeader", "notANumber")
        val result = Await.result(m.toService.apply(request))
        result.status.getCode should be === 400
      }
    }
  }

  def assertOkResponse(module: FintrospectModule2, segments: Seq[String]): Unit = {
    val result = Await.result(module.toService.apply(Request("/svc/" + segments.mkString("/"))))
    result.status.getCode should be === 200
    result.content.toString(Utf8) should be === segments.mkString(",")
  }
}
