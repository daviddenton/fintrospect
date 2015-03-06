package io.github.daviddenton.fintrospect.simple

import org.scalatest.{FunSpec, ShouldMatchers}

class SimpleJsonTest extends FunSpec with ShouldMatchers {

//  describe("simple") {
//    it("renders as expected") {
//      val module = FintrospectModule(Root, SimpleJson)
//        .withRoute(SimpleDescription("a get endpoint", HttpMethod.GET, _ / "echo"), string("message"), (s: String) => null)
//        .withRoute(SimpleDescription("a post endpoint", HttpMethod.POST, _ / "echo"), string("message"), (s: String) => null)
//        .withRoute(SimpleDescription("a friendly endpoint", HttpMethod.GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => null)
//
//      val expected = parse(Source.fromInputStream(this.getClass.getResourceAsStream("expected.json")).mkString)
//      parse(Await.result(module.toService.apply(Request("/"))).content.toString(UTF_8)) should be === expected
//    }
//  }
}
