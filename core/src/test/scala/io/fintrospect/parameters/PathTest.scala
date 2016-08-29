package io.fintrospect.parameters

import com.twitter.finagle.http.Method.Get
import io.fintrospect.RequestBuilder
import org.scalatest.{FunSpec, Matchers}

class PathTest extends FunSpec with Matchers {

  describe("fixed path parameter") {
    it("unapplies when string matches") {
      Path.fixed("a path piece").unapply("a path piece") shouldBe Option("a path piece")
    }

    it("does not unapply when string mismatches") {
      Path.fixed("a path piece").unapply("another path piece") shouldBe None
    }

    it("does not contains any params to describe") {
      Path.fixed("a path piece").iterator.isEmpty shouldBe true
    }
  }

  describe("non fixed parameter") {
    it("does contain a param to describe") {
      Path.string("a path piece").map(_.name) shouldBe Seq("a path piece")
    }

    it("unapplies strings as url decoded values") {
      Path.string("urlEncoded").unapply("a%20path%2F+piece") shouldBe Option("a path/+piece")
    }

    it("does not url decode reserved characters") {
      Path.string("urlEncoded").unapply(":@-._~!$&'()*+,;=") shouldBe Option(":@-._~!$&'()*+,;=")
    }

    it("handles special characters when binding values") {
      (Path.string("urlEncoded") --> "a path/+piece").head.apply(RequestBuilder(Get)).build().uri shouldBe "a%20path%2F+piece"
    }
  }

}