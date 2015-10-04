package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.httpx.Request
import org.scalatest._

class FormFieldTest extends FunSpec with ShouldMatchers {

  private val paramName = "name"

  describe("required") {
    describe("singular") {
      val field = FormField.required.localDate(paramName)

      it("validates value from form field") {
        field.validate(formWithValueOf("2015-02-04")) shouldEqual Right(Option(LocalDate.of(2015, 2, 4)))
        field <-- formWithValueOf("2015-02-04") shouldEqual LocalDate.of(2015, 2, 4)
      }

      it("fails to validate invalid value") {
        field.validate(formWithValueOf("notValid")) shouldEqual Left(field)
      }

      it("does not validate non existent value") {
        field.validate(formWithValueOf()) shouldEqual Left(field)
      }

      it("can rebind valid value") {
        val bindings = FormField.required.int("field") <-> Form(Map("field" -> Set("123")))
        val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
        outForm.get("field") shouldEqual Some(Seq("123"))
      }
    }

    describe("multi") {
      val field = FormField.required.multi.localDate(paramName)

      it("validates value from form field") {
        field.validate(formWithValueOf("2015-02-04", "2015-02-05")) shouldEqual Right(Option(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        field <-- formWithValueOf("2015-02-04", "2015-02-05") shouldEqual Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))
      }

      it("fails to validate invalid value") {
        field.validate(formWithValueOf("2015-02-04", "notValid")) shouldEqual Left(field)
      }

      it("does not validate non existent value") {
        field.validate(formWithValueOf()) shouldEqual Left(field)
      }

      it("can rebind valid value") {
        val bindings = FormField.required.multi.int("field") <-> Form(Map("field" -> Set("123", "456")))
        val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
        outForm.get("field") shouldEqual Some(Seq("123", "456"))
      }
    }
  }

  describe("optional") {
    val field = FormField.optional.localDate(paramName)

    it("validates value from form field") {
      field.validate(formWithValueOf("2015-02-04")) shouldEqual Right(Option(LocalDate.of(2015, 2, 4)))
      field <-- formWithValueOf("2015-02-04") shouldEqual Option(LocalDate.of(2015, 2, 4))
    }

    it("fails to validate invalid value") {
      field.validate(formWithValueOf("notValid")) shouldEqual Left(field)
    }

    it("does not validate non existent value") {
      field.validate(formWithValueOf()) shouldEqual Right(None)
      field <-- formWithValueOf() shouldEqual None
    }

    it("can rebind valid value") {
      val outForm = FormField.optional.int("field") <-> Form(Map("field" -> Set("123")))
      outForm.foldLeft(Form()) { (form, next) => next(form) }.get("field") shouldEqual Some(Seq("123"))
    }

    it("doesn't rebind missing value") {
      val bindings = FormField.optional.int("field") <-> Form()
      val outForm = bindings.foldLeft(Form()) { (requestBuild, next) => next(requestBuild) }
      outForm.get("field") shouldEqual None
    }
  }


  private def requestWithValueOf(value: String*) = {
    Request(value.map(value => paramName -> value): _*)
  }
  private def formWithValueOf(value: String*) = {
    if(value.isEmpty) new Form(Map()) else new Form(Map(paramName -> value.toSet))
  }
}
