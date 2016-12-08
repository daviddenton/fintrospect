package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.io.Buf
import io.fintrospect.util.ExtractionError.{Invalid, Missing}
import io.fintrospect.util.{Extracted, ExtractionFailed}
import org.scalatest._

import scala.language.reflectiveCalls

class FormFieldTest extends FunSpec with Matchers {

  private val paramName = "name"

  describe("required") {
    describe("singular") {
      describe("field") {
        val field = FormField.required.localDate(paramName)

        it("validates value from form field") {
          field.extract(formWithFieldOf("2015-02-04")) shouldBe Extracted(Some(LocalDate.of(2015, 2, 4)))
          field <-- formWithFieldOf("2015-02-04") shouldBe LocalDate.of(2015, 2, 4)
        }

        it("fails to validate invalid value") {
          field.extract(formWithFieldOf("notValid")) shouldBe ExtractionFailed(Invalid(field))
        }

        it("does not validate non existent value") {
          field.extract(formWithFieldOf()) shouldBe ExtractionFailed(Missing(field))
        }

        it("can rebind valid value") {
          val bindings = FormField.required.int("field") <-> new Form(Map("field" -> Set("123")))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.fields.get("field") shouldBe Some(Set("123"))
        }
      }

      describe("file") {
        val file = FormField.required.file(paramName)

        val data = InMemoryMultiPartFile(Buf.Utf8("bob"), None, None)

        it("validates value from form file") {
          file.extract(formWithFileOf(data)) shouldBe Extracted(Some(data))
          file <-- formWithFileOf(data) shouldBe data
        }

        it("does not validate non existent value") {
          file.extract(formWithFileOf()) shouldBe ExtractionFailed(Missing(file))
        }

        it("can rebind valid value") {
          val bindings = FormField.required.file("field") <-> new Form(files = Map("field" -> Set(data)))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.files.get("field") shouldBe Some(Set(data))
        }
      }
    }

    describe("multi") {
      describe("field") {
        val field = FormField.required.multi.localDate(paramName)

        it("validates value from form field") {
          field.extract(formWithFieldOf("2015-02-04", "2015-02-05")) shouldBe Extracted(Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
          field <-- formWithFieldOf("2015-02-04", "2015-02-05") shouldBe Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))
        }

        it("fails to validate invalid value") {
          field.extract(formWithFieldOf("2015-02-04", "notValid")) shouldBe ExtractionFailed(Invalid(field))
        }

        it("does not validate non existent value") {
          field.extract(formWithFieldOf()) shouldBe ExtractionFailed(Missing(field))
        }

        it("can rebind valid value") {
          val bindings = FormField.required.multi.int("field") <-> new Form(Map("field" -> Set("123", "456")))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.fields.get("field") shouldBe Some(Set("123", "456"))
        }
      }

      describe("file") {
        val file = FormField.required.multi.file(paramName)
        val data1 = InMemoryMultiPartFile(Buf.Utf8("bob"), None, None)
        val data2 = InMemoryMultiPartFile(Buf.Utf8("bill"), None, None)

        it("validates value from form file") {
          file.extract(formWithFileOf(data1, data2)) shouldBe Extracted(Some(Seq(data1, data2)))
          file <-- formWithFileOf(data1, data2) shouldBe Seq(data1, data2)
        }

        it("does not validate non existent value") {
          file.extract(formWithFileOf()) shouldBe ExtractionFailed(Missing(file))
        }

        it("can rebind valid value") {
          val bindings = FormField.required.multi.file("field") <-> new Form(files = Map("field" -> Set(data1, data2)))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.files.get("field") shouldBe Some(Set(data1, data2))
        }
      }

    }

    describe("multi-string with empty-is-ok validation turned off") {
      val field = FormField.required.multi.string(paramName, validation = StringValidations.EmptyIsInvalid)

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
        outForm.fields.get("field") shouldBe Some(Set("123", "456"))
      }
    }
  }

  describe("optional") {
    describe("field") {
      val field = FormField.optional.localDate(paramName)

      it("validates value from form field") {
        field.extract(formWithFieldOf("2015-02-04")) shouldBe Extracted(Some(LocalDate.of(2015, 2, 4)))
        field <-- formWithFieldOf("2015-02-04") shouldBe Option(LocalDate.of(2015, 2, 4))
      }

      it("fails to validate invalid value") {
        field.extract(formWithFieldOf("notValid")) shouldBe ExtractionFailed(Invalid(field))
      }

      it("does not validate non existent value") {
        field.extract(formWithFieldOf()) shouldBe Extracted(None)
        field <-- formWithFieldOf() shouldBe None
      }

      it("can rebind valid value") {
        val outForm = FormField.optional.int("field") <-> new Form(Map("field" -> Set("123")))
        outForm.foldLeft(Form()) { (form, next) => next(form) }.fields.get("field") shouldBe Some(Set("123"))
      }

      it("doesn't rebind missing value") {
        val bindings = FormField.optional.int("field") <-> Form()
        val outForm = bindings.foldLeft(Form()) { (requestBuild, next) => next(requestBuild) }
        outForm.fields.get("field") shouldBe None
      }
    }

    describe("file") {
      val file = FormField.optional.file(paramName)

      val data = InMemoryMultiPartFile(Buf.Utf8("bob"), None, None)

      it("validates value from form file") {
        file.extract(formWithFileOf(data)) shouldBe Extracted(Some(data))
        file <-- formWithFileOf(data) shouldBe Some(data)
      }

      it("does not validate non existent value") {
        file.extract(formWithFileOf()) shouldBe Extracted(None)
        file <-- formWithFileOf() shouldBe None
      }

      it("can rebind valid value") {
        val outForm = FormField.optional.file("field") <-> new Form(files = Map("field" -> Set(data)))
        outForm.foldLeft(Form()) { (form, next) => next(form) }.files.get("field") shouldBe Some(Set(data))
      }

      it("doesn't rebind missing value") {
        val bindings = FormField.optional.file("field") <-> Form()
        val outForm = bindings.foldLeft(Form()) { (requestBuild, next) => next(requestBuild) }
        outForm.files.get("field") shouldBe None
      }

    }


  }

  private def formWithFileOf(value: MultiPartFile*) = if (value.isEmpty) new Form() else new Form(files = Map(paramName -> value.toSet))

  private def formWithFieldOf(value: String*) = if (value.isEmpty) new Form() else new Form(Map(paramName -> value.toSet))
}
