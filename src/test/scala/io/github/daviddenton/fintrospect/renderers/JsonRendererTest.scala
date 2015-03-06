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
import util.Echo

import scala.io.Source

abstract class JsonRendererTest(name: String, renderer: FintrospectModule.Renderer) extends FunSpec with ShouldMatchers {
  describe(name) {
    it("renders as expected") {
      val module = FintrospectModule(Root, renderer)
        .withRoute(Description("a get endpoint", HttpMethod.GET, _ / "echo").withHeader("header", classOf[String]), string("message"), (s: String) => Echo(s))
        .withRoute(Description("a post endpoint", HttpMethod.POST, _ / "echo").withBodyParam("bodyParam", classOf[Int]), string("message"), (s: String) => Echo(s))
        .withRoute(Description("a friendly endpoint", HttpMethod.GET, _ / "welcome").withQueryParam("query", classOf[Boolean]), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => Echo(x, y, z))

      val expected = parse(Source.fromInputStream(renderer.getClass.getResourceAsStream(s"$name.json")).mkString)
      val actual = Await.result(module.toService(Request("/"))).content.toString(Utf8)
      parse(actual) should be === expected
    }
  }


}
