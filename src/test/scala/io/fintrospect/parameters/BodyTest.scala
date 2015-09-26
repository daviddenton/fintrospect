package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Request
import com.twitter.io.Charsets
import io.fintrospect.ContentTypes
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import io.fintrospect.util.json.ArgoJsonFormat
import io.fintrospect.util.json.ArgoJsonFormat._
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.jboss.netty.handler.codec.http.HttpMethod
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.xml.XML

class BodyTest extends FunSpec with ShouldMatchers {

  describe("body") {
    it("should retrieve the body value from the request") {
      val bodyJson = obj("field" -> string("value"))
      val request = Request("/")
      request.write(pretty(bodyJson))
      Body.json(Option("description"), obj("field" -> string("value"))) <-- request shouldEqual bodyJson
    }
  }

  describe("form") {
    it("should serialize and deserialize into the request") {
      val date = FormField.required.localDate("date")
      val formBody = Body.form(date)
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31))
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)

      contentFrom(request) shouldEqual "date=1976-08-31"
      request.headers().get(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldEqual inputForm
    }

    it("should serialize strings correctly into the request") {
      val aString = FormField.required.string("na&\"<>me")
      val formBody = Body.form(aString)
      val inputForm = Form(aString --> "&\"<>")
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)

      request.headers().get(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldEqual inputForm
    }

    it("can rebind valid value") {
      val date = FormField.required.localDate("date")
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31))
      val formBody = Body.form(date)
      val bindings = formBody --> inputForm
      val inRequest = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
      val rebindings = formBody <-> inRequest
      val outRequest = rebindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
      val deserializedForm = formBody from outRequest
      deserializedForm shouldEqual inputForm
    }
  }

  describe("json") {
    it("should serialize and deserialize into the request") {

      val jsonBody = Body.json(None)
      val inputJson = obj("bob" -> string("builder"))
      val bindings = jsonBody --> inputJson

      val request = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)

      contentFrom(request) shouldEqual "{\"bob\":\"builder\"}"
      request.headers().get(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_JSON.value
      val deserializedJson = jsonBody <-- request
      deserializedJson shouldEqual inputJson
    }

    it("can rebind valid value") {
      val inRequest = Request()
      val inputJson = obj("bob" -> string("builder"))
      inRequest.setContent(copiedBuffer(pretty(inputJson), Charsets.Utf8))
      val bindings = Body.json(None) <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
      ArgoJsonFormat.parse(contentFrom(outRequest)) shouldEqual inputJson
    }
  }

  describe("xml") {
    it("should serialize and deserialize into the request") {

      val xmlBody = Body.xml(None)
      val inputXml = <field>value</field>
      val bindings = xmlBody --> inputXml

      val request = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)

      contentFrom(request) shouldEqual "<field>value</field>"
      request.headers().get(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_XML.value
      val deserializedXml = xmlBody <-- request
      deserializedXml shouldEqual inputXml
    }

    it("can rebind valid value") {
      val inRequest = Request()
      val inputXml = <field>value</field>
      inRequest.setContent(copiedBuffer(inputXml.toString(), Charsets.Utf8))
      val bindings = Body.xml(None) <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
      XML.loadString(contentFrom(outRequest)) shouldEqual inputXml
    }
  }
}
