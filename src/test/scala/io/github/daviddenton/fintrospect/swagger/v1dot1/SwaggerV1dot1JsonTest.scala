package io.github.daviddenton.fintrospect.swagger.v1dot1

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.io.Charsets._
import com.twitter.util.Await
import io.github.daviddenton.fintrospect.FintrospectModule
import io.github.daviddenton.fintrospect.SegmentMatchers._
import io.github.daviddenton.fintrospect.swagger.Description
import io.github.daviddenton.fintrospect.util.ArgoUtil.parse
import org.jboss.netty.handler.codec.http.HttpMethod
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.io.Source

class SwaggerV1dot1JsonTest extends FunSpec with ShouldMatchers {

  describe("swagger") {
    it("renders as expected") {
      val module = FintrospectModule(Root, Swagger1Renderer)
        .withRoute(Description("a get endpoint", HttpMethod.GET, _ / "echo"), string("message"), (s: String) => null)
        .withRoute(Description("a post endpoint", HttpMethod.POST, _ / "echo"), string("message"), (s: String) => null)
        .withRoute(Description("a friendly endpoint", HttpMethod.GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => null)

      val expected = parse(Source.fromInputStream(this.getClass.getResourceAsStream("expected.json")).mkString)
      parse(Await.result(module.toService.apply(Request("/"))).content.toString(Utf8)) should be === expected
    }
  }
}
