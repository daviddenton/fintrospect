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
  }

  private def formWithValueOf(value: Option[String]) = {
    new Form(value.map(v => collection.Map(paramName -> Set(v))).getOrElse(Map()))
  }
}
