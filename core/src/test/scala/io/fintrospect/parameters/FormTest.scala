package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import io.fintrospect.util.ExtractionError.Missing
import io.fintrospect.util.{Extracted, ExtractionFailed}
import org.scalatest._

class FormTest extends FunSpec with Matchers {

  private val field1 = FormField.required.string("field1")
  private val field2 = FormField.required.string("field2")
  private val field3 = FormField.required.string("field3")
  private val field4 = FormField.required.string("field4")
  private val field5 = FormField.required.string("field5")
  private val field6 = FormField.required.string("field6")

  private val formSpec = Body.form(field1, field2, field3, field4, field5, field6)

  describe("construction") {
    it("from a set of bindings") {
      Form(Seq(new FormFieldBinding(field1, "value"))).iterator.toSeq shouldBe Seq(("field1", Set("value")))
    }
  }

  describe("multiple parameter retrieval") {
    val formInstance = Form(field1 --> "value1", field2 --> "value2", field3 --> "value3", field4 --> "value4", field5 --> "value5", field6 --> "value6")
    it("1 binding") {
      formInstance <-- field1 shouldBe "value1"
      formInstance <-- (field1, field2) shouldBe ("value1", "value2")
      formInstance <-- (field1, field2, field3) shouldBe ("value1", "value2", "value3")
      formInstance <-- (field1, field2, field3, field4) shouldBe ("value1", "value2", "value3", "value4")
      formInstance <-- (field1, field2, field3, field4, field5) shouldBe ("value1", "value2", "value3", "value4", "value5")
      formInstance <-- (field1, field2, field3, field4, field5, field6) shouldBe ("value1", "value2", "value3", "value4", "value5", "value6")
    }
  }

  describe("retrieval") {
    it("handles empty form - optional") {
      val optional = FormField.optional.string("field1")
      Body.form(optional).extract(Request()) shouldBe Extracted(Some(Form()))
      (Body.form(optional) <-- Request()) shouldBe Form()
    }

    it("handles empty form - required") {
      formSpec.extract(Request()) shouldBe ExtractionFailed(Seq(field1, field2, field3, field4, field5, field6).map(f => Missing(f)))
    }
  }
}
