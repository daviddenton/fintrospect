package io.fintrospect.formats

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.Ok
import io.fintrospect.formats.Jackson.JsonFormat._
import io.fintrospect.formats.Jackson._
import io.fintrospect.parameters.{Body, Query}

import scala.language.reflectiveCalls

class JacksonJsonResponseBuilderTest extends JsonResponseBuilderSpec(Jackson)

class JacksonJsonFormatTest extends JsonFormatSpec(Jackson) {
  override val expectedJson: String = """{"string":"hello","null":null,"bigInt":12344,"object":{"field1":"aString"},"decimal":1.2,"double":1.2,"array":["world",true],"long":2,"bool":true,"int":10}"""

  describe("Jackson.JsonFormat") {
    val aLetter = Letter(StreetAddress("my house"), StreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = encode(aLetter)
      decode[Letter](encoded) shouldBe aLetter
    }

    it("empty extracted JSON produces nulls") {
      decode[Letter](Jackson.JsonFormat.obj()) shouldBe Letter(null, null, null)
    }

    it("body spec decodes content") {
      (Body(bodySpec[Letter]()) <-- Jackson.ResponseBuilder.Ok(encode(aLetter)).build()) shouldBe aLetter
    }

    it("response spec has correct code") {
      Jackson.responseSpec[Letter](Ok -> "ok", aLetter).status shouldBe Ok
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[Letter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }
}

