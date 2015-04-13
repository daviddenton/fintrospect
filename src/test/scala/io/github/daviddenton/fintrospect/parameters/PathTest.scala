package io.github.daviddenton.fintrospect.parameters

class PathTest extends ParametersTest[PathParameter](Path) {
  override def from[X](param: PathParameter[X], value: String): Option[X] = {
    param.unapply(value)
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
  }

}