package io.fintrospect.parameters

import org.scalatest._

class FormTest extends FunSpec with ShouldMatchers {

  describe("construction") {
    it("from a set of bindings") {
      val string = FormField.required.string("field")
      Form(Seq(new FormFieldBinding(string, "field", "value"))).iterator.toSeq shouldEqual Seq(("field", Set("value")))
    }
  }
}
