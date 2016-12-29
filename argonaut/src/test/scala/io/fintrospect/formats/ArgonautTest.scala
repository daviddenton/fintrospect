package io.fintrospect.formats

import argonaut.Argonaut.{casecodec1, casecodec3}
import argonaut._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.formats.Argonaut.Filters.{AutoOptionalOut, AutoOut}
import io.fintrospect.formats.Argonaut.JsonFormat._
import io.fintrospect.formats.Argonaut.ResponseBuilder._
import io.fintrospect.formats.Argonaut._
import io.fintrospect.formats.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Body, Query}
import org.scalatest.{FunSpec, Matchers}

import scala.language.reflectiveCalls

case class ArgonautStreetAddress(address: String)

object ArgonautStreetAddress {
  implicit def Codec: CodecJson[ArgonautStreetAddress] = casecodec1(ArgonautStreetAddress.apply, ArgonautStreetAddress.unapply)("address")
}

case class ArgonautLetter(to: ArgonautStreetAddress, from: ArgonautStreetAddress, message: String)

object ArgonautLetter {
  implicit def Codec: CodecJson[ArgonautLetter] = casecodec3(ArgonautLetter.apply, ArgonautLetter.unapply)("to", "from", "message")
}

class ArgonautFiltersTest extends FunSpec with Matchers {

  describe("Argonaut.Filters") {
    val aLetter = ArgonautLetter(ArgonautStreetAddress("my house"), ArgonautStreetAddress("your house"), "hi there")

    val request = Request()
    request.contentString = Argonaut.JsonFormat.compact(encode(aLetter))

    describe("AutoInOut") {
      it("returns Ok") {
        val svc = Filters.AutoInOut(Service.mk { in: ArgonautLetter => Future.value(in) }, Status.Created)

        val response = result(svc(request))
        response.status shouldBe Status.Created
        decode[ArgonautLetter](parse(response.contentString)) shouldBe aLetter
      }
    }

    describe("AutoInOptionalOut") {
      it("returns Ok when present") {
        val svc = Filters.AutoInOptionalOut(Service.mk[ArgonautLetter, Option[ArgonautLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(request))
        response.status shouldBe Status.Ok
        decode[ArgonautLetter](parse(response.contentString)) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = Filters.AutoInOptionalOut(Service.mk[ArgonautLetter, Option[ArgonautLetter]] { in => Future.value(None) })
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

    describe("AutoIn") {
      val svc = Filters.AutoIn(Body(bodySpec[ArgonautLetter]())).andThen(Service.mk { in: ArgonautLetter => Ok(encode(in)) })
      it("takes the object from the request") {
        decode[ArgonautLetter](parse(result(svc(request)).contentString)) shouldBe aLetter
      }

      it("rejects illegal content with a BadRequest") {
        val request = Request()
        request.contentString = "not xml"
        Await.result(svc(request)).status shouldBe Status.BadRequest
      }
    }

    describe("AutoOut") {
      it("takes the object from the request") {
        val svc = AutoOut[ArgonautLetter, ArgonautLetter](Status.Created).andThen(Service.mk { in: ArgonautLetter => Future.value(in) })
        val response = result(svc(aLetter))
        response.status shouldBe Status.Created
        decode[ArgonautLetter](parse(response.contentString)) shouldBe aLetter
      }
    }

    describe("AutoOptionalOut") {
      it("returns Ok when present") {
        val svc = AutoOptionalOut[ArgonautLetter, ArgonautLetter](Status.Created).andThen(Service.mk[ArgonautLetter, Option[ArgonautLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(aLetter))
        response.status shouldBe Status.Created
        decode[ArgonautLetter](parse(response.contentString)) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = AutoOptionalOut[ArgonautLetter, ArgonautLetter](Status.Created).andThen(Service.mk[ArgonautLetter, Option[ArgonautLetter]] { in => Future.value(None) })
        result(svc(aLetter)).status shouldBe Status.NotFound
      }
    }
  }
}

class ArgonautJsonResponseBuilderTest extends JsonResponseBuilderSpec(Argonaut)

class ArgonautJsonFormatTest extends JsonFormatSpec(Argonaut) {

  describe("Argonaut.JsonFormat") {
    val aLetter = ArgonautLetter(ArgonautStreetAddress("my house"), ArgonautStreetAddress("your house"), "hi there")
    it("roundtrips to JSON and back") {
      val encoded = encode(aLetter)(ArgonautLetter.Codec)
      decode[ArgonautLetter](encoded)(ArgonautLetter.Codec) shouldBe aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](decode[ArgonautLetter](obj()))
    }

    it("body spec decodes content") {
      Body(bodySpec[ArgonautLetter]()) <-- Argonaut.ResponseBuilder.Ok(encode(aLetter)).build() shouldBe aLetter
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[ArgonautLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }

    it("response spec has correct code") {
      responseSpec[ArgonautLetter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
    }

  }

  override val expectedJson: String = """{"string":"hello","null":null,"bigInt":12344,"object":{"field1":"aString"},"decimal":1.2,"array":["world",true],"long":2,"bool":true,"int":1}"""
}
