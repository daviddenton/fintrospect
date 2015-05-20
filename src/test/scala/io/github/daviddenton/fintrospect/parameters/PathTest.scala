package io.github.daviddenton.fintrospect.parameters

class PathTest extends ParametersTest[PathParameter](Path) {
  override def from[X](method: (String, String) => PathParameter[X], value: Option[String]): Option[X] = {
    method(paramName, null).unapply(value.orNull)
  }

  describe("fixed path parameter") {
    it("unapplies when string matches") {
      Path.fixed("a path piece").unapply("a path piece") shouldEqual Some("a path piece")
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
      Path.string("a path piece").map(_.name) shouldEqual List("a path piece")
    }

    it("unapplies strings as url decoded values") {
      Path.string("urlEncoded").unapply("a%20path%2F+piece") shouldEqual Some("a path/+piece")
    }

    it("does not url decode reserved characters") {
      Path.string("urlEncoded").unapply(":@-._~!$&'()*+,;=") shouldEqual Some(":@-._~!$&'()*+,;=")
    }
  }

}