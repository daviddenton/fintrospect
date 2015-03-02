package io.github.daviddenton.fintrospect

import java.nio.charset.StandardCharsets.UTF_8

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}
import io.github.daviddenton.fintrospect.SegmentMatchers.{string => s}
import io.github.daviddenton.fintrospect.simple.{SimpleDescription, SimpleJson}
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpMethod
import org.scalatest.{FunSpec, ShouldMatchers}

class FintrospectModuleTest extends FunSpec with ShouldMatchers {

  case class AService(segments: Seq[String]) extends Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val response = Response()
      response.setStatusCode(200)
      response.setContent(copiedBuffer(segments.mkString(","), UTF_8))
      Future.value(response)
    }
  }

  describe("FintrospectModule") {
    describe("Routes a request") {

      val m = FintrospectModule(Root, SimpleJson)
      val d = SimpleDescription("", HttpMethod.GET, _ / "svc")

      it("with 0 segment") {
        assertCorrectResponse(m.withRoute(d, () => AService(Seq())), Seq())
      }
      it("with 1 segments") {
        assertCorrectResponse(m.withRoute(d, s("s1"), (_1: String) => AService(Seq(_1))), Seq("a"))
      }
      it("with 2 segments") {
        assertCorrectResponse(m.withRoute(d, s("s1"), s("s2"), (_1: String, _2: String) => AService(Seq(_1, _2))), Seq("a", "b"))
      }
      it("with 3 segments") {
        assertCorrectResponse(m.withRoute(d, s("s1"), s("s2"), s("s3"), (_1: String, _2: String, _3: String) => AService(Seq(_1, _2, _3))), Seq("a", "b", "c"))
      }
      it("with 4 segments") {
        assertCorrectResponse(m.withRoute(d, s("s1"), s("s2"), s("s3"), s("s4"), (_1: String, _2: String, _3: String, _4: String) => AService(Seq(_1, _2, _3, _4))), Seq("a", "b", "c", "d"))
      }
      it("with 5 segments") {
        assertCorrectResponse(m.withRoute(d, s("s1"), s("s2"), s("s3"), s("s4"), s("s5"), (_1: String, _2: String, _3: String, _4: String, _5: String) => AService(Seq(_1, _2, _3, _4, _5))), Seq("a", "b", "c", "d", "e"))
      }
      it("with 6 segments") {
        assertCorrectResponse(m.withRoute(d, s("s1"), s("s2"), s("s3"), s("s4"), s("s5"), s("s6"), (_1: String, _2: String, _3: String, _4: String, _5: String, _6: String) => AService(Seq(_1, _2, _3, _4, _5, _6))), Seq("a", "b", "c", "d", "e", "f"))
      }
    }
  }

  def assertCorrectResponse(module: FintrospectModule[SimpleDescription], segments: Seq[String]): Unit = {
    val result = Await.result(module.toService.apply(Request("/svc/" + segments.mkString("/"))))
    result.status.getCode should be === 200
    result.content.toString(UTF_8) should be === segments.mkString(",")
  }
}
