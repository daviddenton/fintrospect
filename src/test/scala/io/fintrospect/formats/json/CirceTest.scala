package io.fintrospect.formats.json

import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.Ok
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.formats.json.Circe.JsonFormat._
import io.fintrospect.formats.json.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Body, Query}
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.language.reflectiveCalls

case class CirceStreetAddress(address: String)

case class CirceLetter(to: CirceStreetAddress, from: CirceStreetAddress, message: String)

class CirceJsonResponseBuilderTest extends JsonResponseBuilderSpec(Circe)

class CirceFiltersTest extends FunSpec with ShouldMatchers {

  import io.circe.generic.auto._

  describe("Circe.Filters") {
    val aLetter = CirceLetter(CirceStreetAddress("my house"), CirceStreetAddress("your house"), "hi there")

    val request = Request()
    request.contentString = encode(aLetter).noSpaces

    it("can auto both in and out") {
      val svc = Circe.Filters.AutoInOut(Service.mk { in: CirceLetter => Future.value(in) })
      decode[CirceLetter](parse(result(svc(request)).contentString)) shouldEqual aLetter
    }

//    it("can auto both in and out") {
//      val svc = Circe.Filters.AutoIn() Service.mk { in: CirceLetter => Future.value(in) })
//      decode[CirceLetter](parse(result(svc(request)).contentString)) shouldEqual aLetter
//    }
  }

}

class CirceJsonFormatTest extends JsonFormatSpec(Circe.JsonFormat) {

  import io.circe.generic.auto._

  describe("Circe.JsonFormat") {
    val aLetter = CirceLetter(CirceStreetAddress("my house"), CirceStreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = encode(aLetter)
      decode[CirceLetter](encoded) shouldEqual aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](decode[CirceLetter](Circe.JsonFormat.obj()))
    }

    it("body spec decodes content") {
      (Body(bodySpec[CirceLetter]()) <-- Circe.ResponseBuilder.OK(encode(aLetter)).build()) shouldBe aLetter
    }

    it("response spec has correct code") {
      Circe.JsonFormat.responseSpec[CirceLetter](Ok -> "ok", aLetter).status shouldBe Ok
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[CirceLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }
  override val expectedJson: String = """{"string":"hello","object":{"field1":"aString"},"int":1.0,"long":2,"decimal":1.2,"bigInt":12344,"bool":true,"null":null,"array":["world",true]}"""
}
