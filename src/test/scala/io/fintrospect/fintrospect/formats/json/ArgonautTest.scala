package io.fintrospect.formats.json

import argonaut.Argonaut.{casecodec1, casecodec3}
import argonaut._
import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{Created, Ok}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.formats.json.Argonaut.JsonFormat.{bodySpec, decode, encode, obj, parameterSpec, parse}
import io.fintrospect.formats.json.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Body, Query}
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.language.reflectiveCalls

case class ArgonautStreetAddress(address: String)

object ArgonautStreetAddress {
  implicit def Codec: CodecJson[ArgonautStreetAddress]= casecodec1(ArgonautStreetAddress.apply, ArgonautStreetAddress.unapply)("address")
}

case class ArgonautLetter(to: ArgonautStreetAddress, from: ArgonautStreetAddress, message: String)

object ArgonautLetter {
  implicit def Codec: CodecJson[ArgonautLetter]= casecodec3(ArgonautLetter.apply, ArgonautLetter.unapply)("to", "from", "message")
}


class ArgonautFiltersTest extends FunSpec with ShouldMatchers {

  describe("Argonaut.Filters") {
    val aLetter = ArgonautLetter(ArgonautStreetAddress("my house"), ArgonautStreetAddress("your house"), "hi there")

    val request = Request()
    request.contentString = Argonaut.JsonFormat.compact(encode(aLetter))

    describe("AutoInOut") {
      it("returns Ok") {
        val svc = Argonaut.Filters.AutoInOut(Service.mk { in: ArgonautLetter => Future.value(in) }, Created)

        val response = result(svc(request))
        response.status shouldEqual Created
        decode[ArgonautLetter](parse(response.contentString)) shouldEqual aLetter
      }
    }

    describe("AutoInOptionalOut") {
      it("returns Ok when present") {
        val svc = Argonaut.Filters.AutoInOptionalOut(Service.mk[ArgonautLetter, Option[ArgonautLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(request))
        response.status shouldEqual Ok
        decode[ArgonautLetter](parse(response.contentString)) shouldEqual aLetter
      }

      it("returns NotFound when missing present") {
        val svc = Argonaut.Filters.AutoInOptionalOut(Service.mk[ArgonautLetter, Option[ArgonautLetter]] { in => Future.value(None) })
        result(svc(request)).status shouldEqual Status.NotFound
      }
    }

    describe("AutoIn") {
      it("takes the object from the request") {
        val svc = Argonaut.Filters.AutoIn(Argonaut.JsonFormat.body[ArgonautLetter]()).andThen(Service.mk { in: ArgonautLetter => Future.value(in) })
        result(svc(request)) shouldEqual aLetter
      }
    }

    describe("AutoOut") {
      it("takes the object from the request") {
        val svc = Argonaut.Filters.AutoOut[ArgonautLetter, ArgonautLetter](Created).andThen(Service.mk { in: ArgonautLetter => Future.value(in) })
        val response = result(svc(aLetter))
        response.status shouldEqual Created
        decode[ArgonautLetter](parse(response.contentString)) shouldEqual aLetter
      }
    }

    describe("AutoOptionalOut") {
      it("returns Ok when present") {
        val svc = Argonaut.Filters.AutoOptionalOut[ArgonautLetter, ArgonautLetter](Created).andThen(Service.mk[ArgonautLetter, Option[ArgonautLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(aLetter))
        response.status shouldEqual Created
        decode[ArgonautLetter](parse(response.contentString)) shouldEqual aLetter
      }

      it("returns NotFound when missing present") {
        val svc = Argonaut.Filters.AutoOptionalOut[ArgonautLetter, ArgonautLetter](Created).andThen(Service.mk[ArgonautLetter, Option[ArgonautLetter]] { in => Future.value(None) })
        result(svc(aLetter)).status shouldEqual Status.NotFound
      }
    }
  }
}

class ArgonautJsonResponseBuilderTest extends JsonResponseBuilderSpec(Argonaut)

class ArgonautJsonFormatTest extends JsonFormatSpec(Argonaut.JsonFormat) {

  describe("Argonaut.JsonFormat") {
    val aLetter = ArgonautLetter(ArgonautStreetAddress("my house"), ArgonautStreetAddress("your house"), "hi there")
    it("roundtrips to JSON and back") {
      val encoded = encode(aLetter)(ArgonautLetter.Codec)
      decode[ArgonautLetter](encoded)(ArgonautLetter.Codec) shouldEqual aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](decode[ArgonautLetter](obj()))
    }

    it("body spec decodes content") {
      (Body(bodySpec[ArgonautLetter]()) <-- Argonaut.ResponseBuilder.OK(encode(aLetter)).build()) shouldBe aLetter
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[ArgonautLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }

    it("response spec has correct code") {
      Argonaut.JsonFormat.responseSpec[ArgonautLetter](Ok -> "ok", aLetter).status shouldBe Ok
    }

  }

  override val expectedJson: String = """{"string":"hello","null":null,"bigInt":12344,"object":{"field1":"aString"},"decimal":1.2,"array":["world",true],"long":2,"bool":true,"int":1}"""
}
