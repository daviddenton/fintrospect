package io.fintrospect.parameters

import java.time.LocalDate

import io.fintrospect.parameters.StringValidation.EmptyIsInvalid
import io.fintrospect.util.ExtractionError.{Invalid, Missing}
import io.fintrospect.util.{Extracted, ExtractionFailed}
import org.scalatest._

class FormFieldTest extends FunSpec with Matchers {

  private val paramName = "name"

  describe("required") {
    describe("singular") {
      val field = FormField.required.localDate(paramName)

      it("validates value from form field") {
        field.extract(formWithValueOf("2015-02-04")) shouldBe Extracted(Some(LocalDate.of(2015, 2, 4)))
        field <-- formWithValueOf("2015-02-04") shouldBe LocalDate.of(2015, 2, 4)
      }

      it("fails to validate invalid value") {
        field.extract(formWithValueOf("notValid")) shouldBe ExtractionFailed(Invalid(field))
      }

      it("does not validate non existent value") {
        field.extract(formWithValueOf()) shouldBe ExtractionFailed(Missing(field))
      }

      it("can rebind valid value") {
        val bindings = FormField.required.int("field") <-> new Form(Map("field" -> Set("123")))
        val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
        outForm.get("field") shouldBe Some(Set("123"))
      }
    }

    describe("multi") {
      val field = FormField.required.multi.localDate(paramName)

      it("validates value from form field") {
        field.extract(formWithValueOf("2015-02-04", "2015-02-05")) shouldBe Extracted(Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        field <-- formWithValueOf("2015-02-04", "2015-02-05") shouldBe Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))
      }

      it("fails to validate invalid value") {
        field.extract(formWithValueOf("2015-02-04", "notValid")) shouldBe ExtractionFailed(Invalid(field))
      }

      it("does not validate non existent value") {
        field.extract(formWithValueOf()) shouldBe ExtractionFailed(Missing(field))
      }

      it("can rebind valid value") {
        val bindings = FormField.required.multi.int("field") <-> new Form(Map("field" -> Set("123", "456")))
        val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
        outForm.get("field") shouldBe Some(Set("123", "456"))
      }
    }

    describe("multi-string with empty-is-ok validation turned off") {
      val field = FormField.required.multi.string(paramName, validation = EmptyIsInvalid)

      it("validates value from form field") {
        field.extract(new Form(Map(paramName -> Set("123", "456")))) shouldBe Extracted(Some(Seq("123", "456")))
        field <-- new Form(Map(paramName -> Set("123", "456"))) shouldBe Seq("123", "456")
      }

      it("fails to validate invalid value") {
        field.extract(new Form(Map(paramName -> Set("", "456")))) shouldBe ExtractionFailed(Invalid(field))
      }

      it("does not validate non existent value") {
        field.extract(new Form(Map())) shouldBe ExtractionFailed(Missing(field))
      }

      it("can rebind valid value") {
        val bindings = FormField.required.multi.int("field") <-> new Form(Map("field" -> Set("123", "456")))
        val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
        outForm.get("field") shouldBe Some(Set("123", "456"))
      }
    }
  }

  describe("optional") {
    val field = FormField.optional.localDate(paramName)

    it("validates value from form field") {
      field.extract(formWithValueOf("2015-02-04")) shouldBe Extracted(Some(LocalDate.of(2015, 2, 4)))
      field <-- formWithValueOf("2015-02-04") shouldBe Option(LocalDate.of(2015, 2, 4))
    }

    it("fails to validate invalid value") {
      field.extract(formWithValueOf("notValid")) shouldBe ExtractionFailed(Invalid(field))
    }

    it("does not validate non existent value") {
      field.extract(formWithValueOf()) shouldBe Extracted(None)
      field <-- formWithValueOf() shouldBe None
    }

    it("can rebind valid value") {
      val outForm = FormField.optional.int("field") <-> new Form(Map("field" -> Set("123")))
      outForm.foldLeft(Form()) { (form, next) => next(form) }.get("field") shouldBe Some(Set("123"))
    }

    it("doesn't rebind missing value") {
      val bindings = FormField.optional.int("field") <-> Form()
      val outForm = bindings.foldLeft(Form()) { (requestBuild, next) => next(requestBuild) }
      outForm.get("field") shouldBe None
    }
  }

  private def formWithValueOf(value: String*) = {
    if(value.isEmpty) new Form(Map()) else new Form(Map(paramName -> value.toSet))
  }
}
