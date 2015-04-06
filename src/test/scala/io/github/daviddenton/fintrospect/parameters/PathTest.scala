package io.github.daviddenton.fintrospect.parameters

class PathTest extends ParametersTest[PathParameter](Path) {
  override def from[X](param: PathParameter[X], value: String): Option[X] = {
    param.unapply(value)
  }

  describe("fixed path parameter") {
    it("unapplies when string matches") {
      Path.fixed("a path piece").unapply("a path piece") should be === Some("a path piece")
    }

    it("does not unapply when string mismatches") {
      Path.fixed("a path piece").unapply("another path piece") should be === None
    }

    it("does not contains any params to describe") {
      Path.fixed("a path piece").iterator.isEmpty should be === true
    }
  }

  describe("non path parameter") {
    it("does contain a param to describe") {
      Path.string("a path piece").iterator.next().name should be === "a path piece"
    }
  }

}