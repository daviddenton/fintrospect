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

class FintrospectModuleTest extends FunSpec with ShouldMatchers {

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
      val m = FintrospectModule(Root, SimpleJson())
      val d = Description("")
      val on = On(GET, _ / "svc")

      it("with 0 segment") {
        assertOkResponse(m.withRoute(d, on, () => AService(Seq())), Seq())
      }
      it("with 1 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), (_1: String) => AService(Seq(_1))), Seq("a"))
      }
      it("with 2 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), (_1: String, _2: String) => AService(Seq(_1, _2))), Seq("a", "b"))
      }
      it("with 3 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), string("s3"), (_1: String, _2: String, _3: String) => AService(Seq(_1, _2, _3))), Seq("a", "b", "c"))
      }
      it("with 4 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), string("s3"), string("s4"), (_1: String, _2: String, _3: String, _4: String) => AService(Seq(_1, _2, _3, _4))), Seq("a", "b", "c", "d"))
      }
      it("with 5 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), string("s3"), string("s4"), string("s5"), (_1: String, _2: String, _3: String, _4: String, _5: String) => AService(Seq(_1, _2, _3, _4, _5))), Seq("a", "b", "c", "d", "e"))
      }
      it("with 6 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), string("s3"), string("s4"), string("s5"), string("s6"), (_1: String, _2: String, _3: String, _4: String, _5: String, _6: String) => AService(Seq(_1, _2, _3, _4, _5, _6))), Seq("a", "b", "c", "d", "e", "f"))
      }
    }

    describe("when a route path cannot be found") {
      it("returns a 404") {
        val result = Await.result(FintrospectModule(Root, SimpleJson()).toService.apply(Request("/svc/noSuchRoute")))
        result.status.getCode should be === 404
      }
    }

    describe("Routes valid requests by path") {
      val m = FintrospectModule(Root, SimpleJson())
      val d = Description("")
      val on = On(GET, _ / "svc")

      it("with 0 segment") {
        assertOkResponse(m.withRoute(d, on, () => AService(Seq())), Seq())
      }
      it("with 1 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), (_1: String) => AService(Seq(_1))), Seq("a"))
      }
      it("with 2 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), (_1: String, _2: String) => AService(Seq(_1, _2))), Seq("a", "b"))
      }
      it("with 3 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), string("s3"), (_1: String, _2: String, _3: String) => AService(Seq(_1, _2, _3))), Seq("a", "b", "c"))
      }
      it("with 4 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), string("s3"), string("s4"), (_1: String, _2: String, _3: String, _4: String) => AService(Seq(_1, _2, _3, _4))), Seq("a", "b", "c", "d"))
      }
      it("with 5 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), string("s3"), string("s4"), string("s5"), (_1: String, _2: String, _3: String, _4: String, _5: String) => AService(Seq(_1, _2, _3, _4, _5))), Seq("a", "b", "c", "d", "e"))
      }
      it("with 6 segments") {
        assertOkResponse(m.withRoute(d, on, string("s1"), string("s2"), string("s3"), string("s4"), string("s5"), string("s6"), (_1: String, _2: String, _3: String, _4: String, _5: String, _6: String) => AService(Seq(_1, _2, _3, _4, _5, _6))), Seq("a", "b", "c", "d", "e", "f"))
      }
    }

    describe("when a valid path does not contain all required parameters") {
      val d = Description("").taking(Header.required.int("aNumberHeader"))
      val on = On(GET, _ / "svc")
      val m = FintrospectModule(Root, SimpleJson()).withRoute(d, on, ()=> AService(Seq()))

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

  def assertOkResponse(module: FintrospectModule, segments: Seq[String]): Unit = {
    val result = Await.result(module.toService.apply(Request("/svc/" + segments.mkString("/"))))
    result.status.getCode should be === 200
    result.content.toString(Utf8) should be === segments.mkString(",")
  }
}
