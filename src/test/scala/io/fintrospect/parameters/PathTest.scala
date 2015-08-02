package io.fintrospect.parameters


import org.jboss.netty.handler.codec.http.HttpMethod
import org.scalatest.{FunSpec, ShouldMatchers}

class PathTest extends FunSpec with ShouldMatchers {

  describe("fixed path parameter") {
    it("unapplies when string matches") {
      Path.fixed("a path piece").unapply("a path piece") shouldEqual Option("a path piece")
    }

    it("does not unapply when string mismatches") {
      Path.fixed("a path piece").unapply("another path piece") shouldEqual None
    }

    it("does not contains any params to describe") {
      Path.fixed("a path piece").iterator.isEmpty shouldEqual true
    }
  }

  describe("non fixed parameter") {
    it("does contain a param to describe") {
      Path.string("a path piece").map(_.name) shouldEqual Seq("a path piece")
    }

    it("unapplies strings as url decoded values") {
      Path.string("urlEncoded").unapply("a%20path%2F+piece") shouldEqual Option("a path/+piece")
    }

    it("does not url decode reserved characters") {
      Path.string("urlEncoded").unapply(":@-._~!$&'()*+,;=") shouldEqual Option(":@-._~!$&'()*+,;=")
    }

    it("handles special characters when binding values") {
      (Path.string("urlEncoded") --> "a path/+piece").head.apply(RequestBuild()).build(HttpMethod.GET).getUri shouldEqual "a%20path%2F+piece"
    }
  }

}