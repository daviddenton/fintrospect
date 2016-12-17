package io.fintrospect.formats

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.Ok
import io.fintrospect.formats.Jackson.JsonFormat._
import io.fintrospect.formats.Jackson._
import io.fintrospect.parameters.{Body, Query}

import scala.language.reflectiveCalls

case class JacksonStreetAddress(address: String)

case class JacksonLetter(to: JacksonStreetAddress, from: JacksonStreetAddress, message: String)

case class JacksonLetterOpt(to: JacksonStreetAddress, from: JacksonStreetAddress, message: Option[String])

class JacksonJsonResponseBuilderTest extends JsonResponseBuilderSpec(Jackson)

class JacksonJsonFormatTest extends JsonFormatSpec(Jackson) {
  override val expectedJson: String = """{"string":"hello","null":null,"bigInt":12344,"object":{"field1":"aString"},"decimal":1.2,"array":["world",true],"long":2,"bool":true,"int":1}"""

  describe("Jackson.JsonFormat") {
    val aLetter = JacksonLetter(JacksonStreetAddress("my house"), JacksonStreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = encode(aLetter)
      decode[JacksonLetter](encoded) shouldBe aLetter
    }

    it("empty extracted JSON produces nulls") {
      decode[JacksonLetter](Jackson.JsonFormat.obj()) shouldBe JacksonLetter(null, null, null)
    }

    it("body spec decodes content") {
      (Body(bodySpec[JacksonLetter]()) <-- Jackson.ResponseBuilder.OK(encode(aLetter)).build()) shouldBe aLetter
    }

    it("response spec has correct code") {
      Jackson.responseSpec[JacksonLetter](Ok -> "ok", aLetter).status shouldBe Ok
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[JacksonLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }
}

