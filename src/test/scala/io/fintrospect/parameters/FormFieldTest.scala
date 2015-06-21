package io.fintrospect.parameters

import java.time.LocalDate

import org.scalatest._

class FormFieldTest extends FunSpec with ShouldMatchers {

  private val paramName = "name"

  describe("required") {
    val field = FormField.required(ParameterSpec.localDate(paramName))

    it("validates value from form field") {
      field.validate(formWithValueOf(Some("2015-02-04"))) shouldEqual Right(Some(LocalDate.of(2015, 2, 4)))
      field.from(formWithValueOf(Some("2015-02-04"))) shouldEqual LocalDate.of(2015, 2, 4)
    }

    it("fails to validate invalid value") {
      field.validate(formWithValueOf(Some("notValid"))) shouldEqual Left(field)
    }

    it("does not validate non existent value") {
      field.validate(formWithValueOf(None)) shouldEqual Left(field)
    }
  }

  describe("optional") {
    val field = FormField.optional(ParameterSpec.localDate(paramName))

    it("validates value from form field") {
      field.validate(formWithValueOf(Some("2015-02-04"))) shouldEqual Right(Some(LocalDate.of(2015, 2, 4)))
      field.from(formWithValueOf(Some("2015-02-04"))) shouldEqual Some(LocalDate.of(2015, 2, 4))
    }

    it("fails to validate invalid value") {
      field.validate(formWithValueOf(Some("notValid"))) shouldEqual Left(field)
    }

    it("does not validate non existent value") {
      field.validate(formWithValueOf(None)) shouldEqual Right(None)
      field.from(formWithValueOf(None)) shouldEqual None
    }
  }

  private def formWithValueOf(value: Option[String]) = {
    new NewForm(value.map(v => collection.Map(paramName -> Set(v))).getOrElse(Map()))
  }
}
