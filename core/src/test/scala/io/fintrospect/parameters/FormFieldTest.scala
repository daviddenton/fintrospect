package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.io.Buf
import io.fintrospect.util.ExtractionError.{Invalid, Missing}
import io.fintrospect.util.{Extracted, ExtractionFailed}
import org.scalatest._



class FormFieldTest extends FunSpec with Matchers {

  private val paramName = "name"

  describe("required") {
    describe("singular") {
      describe("field") {
        val field = FormField.required.localDate(paramName)

        it("validates value from form field") {
          field.extract(formWithFieldOf("2015-02-04")) shouldBe Extracted(LocalDate.of(2015, 2, 4))
          field <-- formWithFieldOf("2015-02-04") shouldBe LocalDate.of(2015, 2, 4)
        }

        it("fails to validate invalid value") {
          field.extract(formWithFieldOf("notValid")) shouldBe ExtractionFailed(Invalid(field))
        }

        it("does not validate non existent value") {
          field.extract(formWithFieldOf()) shouldBe ExtractionFailed(Missing(field))
        }

        it("can rebind valid value") {
          val bindings = FormField.required.int("field") <-> new Form(Map("field" -> Seq("123")))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.fields.get("field") shouldBe Some(Seq("123"))
        }
      }

      describe("file") {
        val file = FormField.required.file(paramName)

        val data = InMemoryMultiPartFile("file", Buf.Utf8("bob"), None)

        it("validates value from form file") {
          file.extract(formWithFileOf(data)) shouldBe Extracted(data)
          file <-- formWithFileOf(data) shouldBe data
        }

        it("does not validate non existent value") {
          file.extract(formWithFileOf()) shouldBe ExtractionFailed(Missing(file))
        }

        it("can rebind valid value") {
          val bindings = FormField.required.file("field") <-> new Form(files = Map("field" -> Seq(data)))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.files.get("field") shouldBe Some(Seq(data))
        }
      }
    }

    describe("multi") {
      describe("field") {
        val field = FormField.required.*.localDate(paramName)

        it("validates value from form field") {
          field.extract(formWithFieldOf("2015-02-04", "2015-02-05")) shouldBe Extracted(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5)))
          field <-- formWithFieldOf("2015-02-04", "2015-02-05") shouldBe Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))
        }

        it("fails to validate invalid value") {
          field.extract(formWithFieldOf("2015-02-04", "notValid")) shouldBe ExtractionFailed(Invalid(field))
        }

        it("does not validate non existent value") {
          field.extract(formWithFieldOf()) shouldBe ExtractionFailed(Missing(field))
        }

        it("can rebind valid value") {
          val bindings = FormField.required.*.int("field") <-> new Form(Map("field" -> Seq("123", "456")))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.fields.get("field") shouldBe Some(Seq("456", "123"))
        }
      }

      describe("file") {
        val file = FormField.required.*.file(paramName)
        val data1 = InMemoryMultiPartFile("file", Buf.Utf8("bob"), None)
        val data2 = InMemoryMultiPartFile("file2", Buf.Utf8("bill"), None)

        it("validates value from form file") {
          file.extract(formWithFileOf(data1, data2)) shouldBe Extracted(Seq(data1, data2))
          file <-- formWithFileOf(data1, data2) shouldBe Seq(data1, data2)
        }

        it("does not validate non existent value") {
          file.extract(formWithFileOf()) shouldBe ExtractionFailed(Missing(file))
        }

        it("can rebind valid value") {
          val bindings = FormField.required.*.file("field") <-> new Form(files = Map("field" -> Seq(data1, data2)))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.files.get("field") shouldBe Some(Seq(data2, data1))
        }
      }
    }

    describe("multi-string with empty-is-ok validation turned off") {
      val field = FormField.required.*.string(paramName)

      it("validates value from form field") {
        field.extract(new Form(Map(paramName -> Seq("123", "456")))) shouldBe Extracted(Seq("123", "456"))
        field <-- new Form(Map(paramName -> Seq("123", "456"))) shouldBe Seq("123", "456")
      }

      it("fails to validate invalid value") {
        field.extract(new Form(Map(paramName -> Seq("", "456")))) shouldBe ExtractionFailed(Invalid(field))
      }

      it("does not validate non existent value") {
        field.extract(new Form(Map())) shouldBe ExtractionFailed(Missing(field))
      }

      it("can rebind valid value") {
        val bindings = FormField.required.*.int("field") <-> new Form(Map("field" -> Seq("123", "456")))
        val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
        outForm.fields.get("field") shouldBe Some(Seq("456", "123"))
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
        val outForm = FormField.optional.int("field") <-> new Form(Map("field" -> Seq("123")))
        outForm.foldLeft(Form()) { (form, next) => next(form) }.fields.get("field") shouldBe Some(Seq("123"))
      }

      it("doesn't rebind missing value") {
        val bindings = FormField.optional.int("field") <-> Form()
        val outForm = bindings.foldLeft(Form()) { (requestBuild, next) => next(requestBuild) }
        outForm.fields.get("field") shouldBe None
      }
    }

    describe("file") {
      val file = FormField.optional.file(paramName)

      val data = InMemoryMultiPartFile("file", Buf.Utf8("bob"), None)

      it("validates value from form file") {
        file.extract(formWithFileOf(data)) shouldBe Extracted(Some(data))
        file <-- formWithFileOf(data) shouldBe Some(data)
      }

      it("does not validate non existent value") {
        file.extract(formWithFileOf()) shouldBe Extracted(None)
        file <-- formWithFileOf() shouldBe None
      }

      it("can rebind valid value") {
        val outForm = FormField.optional.file("field") <-> new Form(files = Map("field" -> Seq(data)))
        outForm.foldLeft(Form()) { (form, next) => next(form) }.files.get("field") shouldBe Some(Seq(data))
      }

      it("doesn't rebind missing value") {
        val bindings = FormField.optional.file("field") <-> Form()
        val outForm = bindings.foldLeft(Form()) { (requestBuild, next) => next(requestBuild) }
        outForm.files.get("field") shouldBe None
      }
    }

    describe("multi") {
      describe("field") {
        val field = FormField.optional.*.localDate(paramName)

        it("validates value from form field") {
          field.extract(formWithFieldOf("2015-02-04", "2015-02-05")) shouldBe Extracted(Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
          field <-- formWithFieldOf("2015-02-04", "2015-02-05") shouldBe Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5)))
        }

        it("fails to validate invalid value") {
          field.extract(formWithFieldOf("2015-02-04", "notValid")) shouldBe ExtractionFailed(Invalid(field))
        }

        it("does not validate non existent value") {
          field.extract(formWithFieldOf()) shouldBe Extracted(None)
        }

        it("can rebind valid value") {
          val bindings = FormField.optional.*.int("field") <-> new Form(Map("field" -> Seq("123", "456")))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.fields.get("field") shouldBe Some(Seq("456", "123"))
        }
      }

      describe("file") {
        val file = FormField.optional.*.file(paramName)
        val data1 = InMemoryMultiPartFile("file", Buf.Utf8("bob"), None)
        val data2 = InMemoryMultiPartFile("file2", Buf.Utf8("bill"), None)

        it("validates value from form file") {
          file.extract(formWithFileOf(data1, data2)) shouldBe Extracted(Some(Seq(data1, data2)))
          file <-- formWithFileOf(data1, data2) shouldBe Some(Seq(data1, data2))
        }

        it("does not validate non existent value") {
          file.extract(formWithFileOf()) shouldBe Extracted(None)
        }

        it("can rebind valid value") {
          val bindings = FormField.optional.*.file("field") <-> new Form(files = Map("field" -> Seq(data1, data2)))
          val outForm = bindings.foldLeft(Form()) { (form, next) => next(form) }
          outForm.files.get("field") shouldBe Some(Seq(data2, data1))
        }
      }
    }
  }

  private def formWithFileOf(value: MultiPartFile*) = if (value.isEmpty) new Form() else new Form(files = Map(paramName -> value.toSeq))

  private def formWithFieldOf(value: String*) = if (value.isEmpty) new Form() else new Form(Map(paramName -> value.toSeq))
}
