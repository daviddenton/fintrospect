package io.fintrospect.parameters

import java.time.LocalDate

import org.scalatest._

class FormFieldTest extends FunSpec with ShouldMatchers {

  private val paramName = "name"

  describe("required") {
    val field = FormField.required.localDate(paramName)

    it("validates value from form field") {
      field.validate(formWithValueOf(Option("2015-02-04"))) shouldEqual Right(Option(LocalDate.of(2015, 2, 4)))
      field <-- formWithValueOf(Option("2015-02-04")) shouldEqual LocalDate.of(2015, 2, 4)
    }

    it("fails to validate invalid value") {
      field.validate(formWithValueOf(Option("notValid"))) shouldEqual Left(field)
    }

    it("does not validate non existent value") {
      field.validate(formWithValueOf(None)) shouldEqual Left(field)
    }

    it("can rebind valid value") {
      val bindings = FormField.required.int("field") <-> Form(Map("field" -> Set("123")))
      val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
      outForm.get("field") shouldEqual Some("123")
    }
  }

  describe("optional") {
    val field = FormField.optional.localDate(paramName)

    it("validates value from form field") {
      field.validate(formWithValueOf(Option("2015-02-04"))) shouldEqual Right(Option(LocalDate.of(2015, 2, 4)))
      field <-- formWithValueOf(Option("2015-02-04")) shouldEqual Option(LocalDate.of(2015, 2, 4))
    }

    it("fails to validate invalid value") {
      field.validate(formWithValueOf(Option("notValid"))) shouldEqual Left(field)
    }

    it("does not validate non existent value") {
      field.validate(formWithValueOf(None)) shouldEqual Right(None)
      field <-- formWithValueOf(None) shouldEqual None
    }

    it("can rebind valid value") {
      val outForm = FormField.optional.int("field") <-> Form(Map("field" -> Set("123")))
      outForm.foldLeft(Form()) { (form, next) => next(form) }.get("field") shouldEqual Some("123")
    }

    it("doesn't rebind missing value") {
      val bindings = FormField.optional.int("field") <-> Form()
      val outForm = bindings.foldLeft(Form()) { (requestBuild, next) => next(requestBuild) }
      outForm.get("field") shouldEqual None
    }
  }

  private def formWithValueOf(value: Option[String]) = {
    new Form(value.map(v => collection.Map(paramName -> Set(v))).getOrElse(Map()))
  }
}
