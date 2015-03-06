package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.io.Charsets._
import com.twitter.util.Await
import io.github.daviddenton.fintrospect.SegmentMatchers._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.{Description, FintrospectModule}
import org.jboss.netty.handler.codec.http.HttpMethod
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.io.Source

abstract class JsonRendererTest(name: String, renderer: FintrospectModule.Renderer) extends FunSpec with ShouldMatchers {
  describe(name) {
    it("renders as expected") {
      val module = FintrospectModule(Root, renderer)
        .withRoute(Description("a get endpoint", HttpMethod.GET, _ / "echo").withHeader("aHeader", classOf[String]), string("message"), (s: String) => null)
        .withRoute(Description("a post endpoint", HttpMethod.POST, _ / "echo"), string("message"), (s: String) => null)
        .withRoute(Description("a friendly endpoint", HttpMethod.GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => null)

      val expected = parse(Source.fromInputStream(renderer.getClass.getResourceAsStream(s"$name.json")).mkString)
      parse(Await.result(module.toService.apply(Request("/"))).content.toString(Utf8)) should be === expected
    }
  }


}
