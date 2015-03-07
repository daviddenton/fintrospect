package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.io.Charsets._
import com.twitter.util.Await
import io.github.daviddenton.fintrospect.Locations.{Header, Query}
import io.github.daviddenton.fintrospect.SegmentMatchers.{string, _}
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.{Description, FintrospectModule, Parameters}
import org.jboss.netty.handler.codec.http.HttpMethod
import org.scalatest.{FunSpec, ShouldMatchers}
import util.Echo

import scala.io.Source

abstract class JsonRendererTest(name: String, renderer: FintrospectModule.Renderer) extends FunSpec with ShouldMatchers {
  describe(name) {
    it("renders as expected") {
      val module = FintrospectModule(Root, renderer)
        .withRoute(Description("a get endpoint", HttpMethod.GET, _ / "echo").requiring(Parameters.string(Header, "header")), string("message"), (s: String) => Echo(s))
        .withRoute(Description("a post endpoint", HttpMethod.POST, _ / "echo").requiring(Parameters.int(Query, "query")), string("message"), (s: String) => Echo(s))
        .withRoute(Description("a friendly endpoint", HttpMethod.GET, _ / "welcome").requiring(Parameters.boolean(Query, "query")), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => Echo(x, y, z))

      val expected = parse(Source.fromInputStream(renderer.getClass.getResourceAsStream(s"$name.json")).mkString)
      val actual = Await.result(module.toService(Request("/"))).content.toString(Utf8)
//      println(actual)
      parse(actual) should be === expected
    }
  }


}
