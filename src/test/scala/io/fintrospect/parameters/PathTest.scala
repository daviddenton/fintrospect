package io.fintrospect.parameters

class PathTest extends ParametersTest[PathParameter, Retrieval](Path) {
  override def from[X](method: (String, String) => PathParameter[X] with Retrieval[X], value: Option[String])= {
    method(paramName, null).unapply(value.orNull)
  }

  override def to[X](method: (String, String) => PathParameter[X] with Retrieval[X], value: X): ParamBinding[X] = method(paramName, null) -> value

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