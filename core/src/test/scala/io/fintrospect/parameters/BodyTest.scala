package io.fintrospect.parameters

import java.time.LocalDate

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.io.{Buf, Bufs}
import io.fintrospect.ContentTypes.MULTIPART_FORM
import io.fintrospect.formats.Argo
import io.fintrospect.formats.Argo.JsonFormat.{obj, pretty, string}
import io.fintrospect.util.ExtractionError.Invalid
import io.fintrospect.util.{Extracted, ExtractionError, ExtractionFailed}
import io.fintrospect.{ContentType, ContentTypes, RequestBuilder}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE
import org.scalatest.{FunSpec, Matchers}

import scala.language.reflectiveCalls
import scala.xml.XML

class BodyTest extends FunSpec with Matchers {

  describe("body") {

    describe("json") {
      val body = Body.json("description", obj("field" -> string("value")))

      it("should retrieve the body value from the request") {
        val bodyJson = obj("field" -> string("value"))
        val request = Request("/")
        request.write(pretty(bodyJson))
        body.extract(request) shouldBe Extracted(Some(bodyJson))
        body <-- request shouldBe bodyJson
      }

      it("should retrieve the body value from the pre-extracted Request") {
        val bodyJson = obj("field" -> string("value"))
        val request = Request("/")
        body.extract(ExtractedRouteRequest(request, Map(body -> Extracted(Some(bodyJson))))) shouldBe Extracted(Some(bodyJson))
      }

      it("validation when missing") {
        body.extract(Request()) shouldBe ExtractionFailed(body.iterator.toSeq.map(p => Invalid(p)))
      }
    }
  }

  describe("string") {
    val body = Body.string("description", ContentType("sometype"))

    it("should retrieve the body value from the request") {
      val request = Request("/")
      request.write("hello")
      body.extract(request) shouldBe Extracted(Some("hello"))
      body <-- request shouldBe "hello"
    }

    it("should retrieve the body value from the pre-extracted Request") {
      val request = Request("/")
      body.extract(ExtractedRouteRequest(request, Map(body -> Extracted(Some("hello"))))) shouldBe Extracted(Some("hello"))
    }

    it("defaults to empty is invalid") {
      body.extract(Request()) shouldBe ExtractionFailed(body.iterator.toSeq.map(p => Invalid(p)))
    }

    it("can override to empty is valid") {
      Body.string("description", ContentType("sometype"), StringValidations.EmptyIsValid).extract(Request()) shouldBe Extracted(Some(""))
    }
  }

  describe("form") {
    it("handles empty fields") {
      val string = FormField.required.string("aString", null, StringValidations.EmptyIsValid)
      val formBody = Body.form(string)
      val inputForm = Form(string --> "")
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.contentString shouldBe "aString="
      request.headerMap(CONTENT_TYPE) shouldBe ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldBe inputForm
    }

    it("should serialize and deserialize into the request") {
      val date = FormField.required.localDate("date")
      val formBody = Body.form(date)
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31))
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.contentString shouldBe "date=1976-08-31"
      request.headerMap(CONTENT_TYPE) shouldBe ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldBe inputForm
    }

    it("should serialize strings correctly into the request") {
      val aString = FormField.required.string("na&\"<>me")
      val formBody = Body.form(aString)
      val inputForm = Form(aString --> "&\"<>")
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.headerMap(CONTENT_TYPE) shouldBe ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldBe inputForm
    }

    it("can rebind valid value") {
      val date = FormField.required.localDate("date")
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31))
      val formBody = Body.form(date)
      val bindings = formBody --> inputForm
      val inRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      val rebindings = formBody <-> inRequest
      val outRequest = rebindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      val deserializedForm = formBody from outRequest
      deserializedForm shouldBe inputForm
    }
  }

  describe("webform") {
    it("collects valid and invalid fields from the request, and maps error fields to custom messages") {
      val optional = FormField.optional.string("anOption")
      val string = FormField.required.string("aString")
      val anotherString = FormField.required.string("anotherString")
      val formBody = Body.webForm(optional -> "Custom", string -> "Custom", anotherString -> "Custom")
      val inputForm = Form(string --> "asd")
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.contentString shouldBe "aString=asd"
      request.headerMap(CONTENT_TYPE) shouldBe ContentTypes.APPLICATION_FORM_URLENCODED.value
      formBody from request shouldBe new Form(inputForm.fields, Map.empty, Seq(ExtractionError(anotherString, "Custom")))
    }
  }

  describe("multipartform") {
    it("should serialize and deserialize into the request") {
      val date = FormField.required.localDate("date")
      val file = FormField.required.file("file")
      val formBody = Body.multiPartForm(date, file)
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31), file --> InMemoryMultiPartFile("file", Buf.Utf8("bob"), Option("type")))
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.headerMap(CONTENT_TYPE).startsWith(MULTIPART_FORM.value) shouldBe true
      val deserializedForm = formBody from request
      date <-- deserializedForm shouldBe LocalDate.of(1976, 8, 31)
      Bufs.asUtf8String((file <-- deserializedForm).asInstanceOf[InMemoryMultiPartFile].content) shouldBe "bob"
    }

    it("can rebind valid value") {
      val date = FormField.required.localDate("date")
      val file = FormField.required.file("file")
      val formBody = Body.multiPartForm(date, file)
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31), file --> InMemoryMultiPartFile("file", Buf.Utf8("bob"), None))
      val bindings = formBody --> inputForm
      val inRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      val rebindings = formBody <-> inRequest
      val outRequest = rebindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      val deserializedForm = formBody from outRequest
      date <-- deserializedForm shouldBe LocalDate.of(1976, 8, 31)
      Bufs.asUtf8String((file <-- deserializedForm).asInstanceOf[InMemoryMultiPartFile].content) shouldBe "bob"
    }

    it("filters out empty files") {
      val date = FormField.required.localDate("date")
      val file = FormField.optional.file("file")
      val formBody = Body.multiPartForm(date, file)
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31), file --> InMemoryMultiPartFile("", Buf.Utf8(""), None))
      val bindings = formBody --> inputForm
      val inRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      val rebindings = formBody <-> inRequest
      val outRequest = rebindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      val deserializedForm = formBody <-- outRequest
      (file <-- deserializedForm).isEmpty shouldBe true
    }
  }

  describe("multipartwebform") {
    it("collects valid and invalid fields from the request, and maps error fields to custom messages") {
      val optional = FormField.optional.string("anOption")
      val string = FormField.required.string("aString")
      val file = FormField.required.multi.file("aFile")
      val formBody = Body.multiPartWebForm(optional -> "Custom", string -> "Custom", file -> "Custom")
      val inputForm = Form(string --> "asd")
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.headerMap(CONTENT_TYPE).startsWith(MULTIPART_FORM.value) shouldBe true
      formBody from request shouldBe new Form(inputForm.fields, Map.empty, Seq(ExtractionError(file, "Custom")))
    }
  }

  describe("json") {
    it("should serialize and deserialize into the request") {

      val jsonBody = Body.json[JsonRootNode]()
      val inputJson = obj("bob" -> string("builder"))
      val bindings = jsonBody --> inputJson

      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.contentString shouldBe "{\"bob\":\"builder\"}"
      request.headerMap(CONTENT_TYPE) shouldBe ContentTypes.APPLICATION_JSON.value
      val deserializedJson = jsonBody <-- request
      deserializedJson shouldBe inputJson
    }

    it("can rebind valid value") {
      val inRequest = Request()
      val inputJson = obj("bob" -> string("builder"))
      inRequest.setContentString(pretty(inputJson))

      val bindings = Body.json() <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      Argo.JsonFormat.parse(outRequest.contentString) shouldBe inputJson
    }
  }

  describe("xml") {
    it("should serialize and deserialize into the request") {

      val xmlBody = Body.xml()
      val inputXml = <field>value</field>
      val bindings = xmlBody --> inputXml

      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.contentString shouldBe "<field>value</field>"
      request.headerMap(CONTENT_TYPE) shouldBe ContentTypes.APPLICATION_XML.value
      val deserializedXml = xmlBody <-- request
      deserializedXml shouldBe inputXml
    }

    it("can rebind valid value") {
      val inRequest = Request()
      val inputXml = <field>value</field>
      inRequest.setContentString(inputXml.toString())
      val bindings = Body.xml() <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      XML.loadString(outRequest.contentString) shouldBe inputXml
    }
  }

  describe("binary") {
    val contentType = ContentType("app/exe")
    val binaryBody = Body.binary("exe", contentType)
    val input = Buf.ByteArray("test".getBytes: _*)

    it("should serialize and deserialize into the request") {
      val bindings = binaryBody --> input

      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.content shouldBe input
      request.headerMap(CONTENT_TYPE) shouldBe contentType.value
      val deserializedBinary = binaryBody <-- request
      deserializedBinary shouldBe input
    }

    it("can rebind valid value") {
      val inRequest = Request()
      inRequest.content = input
      val bindings = binaryBody <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      outRequest.content shouldBe input
    }
  }
}
