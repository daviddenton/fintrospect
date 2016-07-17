package io.fintrospect.parameters

import java.time.LocalDate

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import io.fintrospect.formats.json.Argo
import io.fintrospect.formats.json.Argo.JsonFormat.{obj, pretty, string}
import io.fintrospect.util.{Extracted, ExtractionError, ExtractionFailed}
import io.fintrospect.{ContentTypes, RequestBuilder}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.xml.XML

class BodyTest extends FunSpec with ShouldMatchers {

  describe("body") {

    val body = Body.json(Option("description"), obj("field" -> string("value")))

    it("should retrieve the body value from the request") {
      val bodyJson = obj("field" -> string("value"))
      val request = Request("/")
      request.write(pretty(bodyJson))
      body.extract(request) shouldEqual Extracted(Some(bodyJson))
      body <-- request shouldEqual bodyJson
    }

    it("validation when missing") {
      body.extract(Request()) shouldEqual ExtractionFailed(body.iterator.toSeq.map(p => ExtractionError.Invalid(p.name)))
    }
  }

  describe("form") {
    it("handles empty fields") {
      val string = FormField.required.string("aString")
      val formBody = Body.form(string)
      val inputForm = Form(string --> "")
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.contentString shouldEqual "aString="
      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldEqual inputForm
    }

    it("should serialize and deserialize into the request") {
      val date = FormField.required.localDate("date")
      val formBody = Body.form(date)
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31))
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.contentString shouldEqual "date=1976-08-31"
      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldEqual inputForm
    }

    it("should serialize strings correctly into the request") {
      val aString = FormField.required.string("na&\"<>me")
      val formBody = Body.form(aString)
      val inputForm = Form(aString --> "&\"<>")
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldEqual inputForm
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
      deserializedForm shouldEqual inputForm
    }
  }
//
//  describe("Webform") {
//    it("collects valid and invalid fields from the request") {
//      val optional = FormField.optional.string("anOption")
//      val string = FormField.required.string("aString")
//      val anotherString = FormField.required.string("anotherString")
//      val formBody = Body.webForm(optional, string, anotherString)
//      val inputForm = Form(string --> "asd")
//      val bindings = formBody --> inputForm
//      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
//
//      request.contentString shouldEqual "aString=asd"
//      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_FORM_URLENCODED.value
//      val deserializedForm = formBody from request
//      deserializedForm shouldEqual WebForm(inputForm, Seq(ExtractionError.Missing(anotherString.name)))
//    }
//  }

  describe("json") {
    it("should serialize and deserialize into the request") {

      val jsonBody = Body.json[JsonRootNode](None)
      val inputJson = obj("bob" -> string("builder"))
      val bindings = jsonBody --> inputJson

      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.contentString shouldEqual "{\"bob\":\"builder\"}"
      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_JSON.value
      val deserializedJson = jsonBody <-- request
      deserializedJson shouldEqual inputJson
    }

    it("can rebind valid value") {
      val inRequest = Request()
      val inputJson = obj("bob" -> string("builder"))
      inRequest.setContentString(pretty(inputJson))

      val bindings = Body.json(None) <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      Argo.JsonFormat.parse(outRequest.contentString) shouldEqual inputJson
    }
  }

  describe("xml") {
    it("should serialize and deserialize into the request") {

      val xmlBody = Body.xml(None)
      val inputXml = <field>value</field>
      val bindings = xmlBody --> inputXml

      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.contentString shouldEqual "<field>value</field>"
      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_XML.value
      val deserializedXml = xmlBody <-- request
      deserializedXml shouldEqual inputXml
    }

    it("can rebind valid value") {
      val inRequest = Request()
      val inputXml = <field>value</field>
      inRequest.setContentString(inputXml.toString())
      val bindings = Body.xml(None) <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      XML.loadString(outRequest.contentString) shouldEqual inputXml
    }
  }
}
