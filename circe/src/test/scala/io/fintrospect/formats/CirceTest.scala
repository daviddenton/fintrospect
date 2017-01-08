package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Status}
import com.twitter.io.{Buf, Bufs}
import com.twitter.util.Future
import io.circe.generic.auto._
import io.fintrospect.formats.Circe.Auto._
import io.fintrospect.formats.Circe.JsonFormat._
import io.fintrospect.formats.Circe._
import io.fintrospect.formats.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Body, BodySpec, Query}

import scala.language.reflectiveCalls

class CirceJsonResponseBuilderTest extends JsonResponseBuilderSpec(Circe)

class CirceAutoTest extends AutoSpec(Circe.Auto) {

  describe("API") {
    it("can find implicits") {
      Circe.Auto.InOut[Letter, Letter](Service.mk { in: Letter => Future(in) })
    }
  }


  override def toBuf(l: Letter) = Bufs.utf8Buf(encode(l).noSpaces)

  override def fromBuf(s: Buf): Letter = decode[Letter](parse(Bufs.asUtf8String(s)))

  override def bodySpec: BodySpec[Letter] = Circe.bodySpec[Letter]()

  override def transform() = Circe.Auto.tToJson[Letter]
}

class CirceJsonFormatTest extends JsonFormatSpec(Circe) {

  import io.circe.generic.auto._

  describe("Circe.JsonFormat") {
    val aLetter = Letter(StreetAddress("my house"), StreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = encode(aLetter)
      decode[Letter](encoded) shouldBe aLetter
    }

    it("patchbody modifies original object with a non-null value") {
      val original = LetterOpt(StreetAddress("my house"), StreetAddress("your house"), None)
      val modifier = encode(obj("message" -> string("hi there")))
      val modifyLetter = patcher[LetterOpt](modifier)
      modifyLetter(original) shouldBe LetterOpt(StreetAddress("my house"), StreetAddress("your house"), Option("hi there"))
    }

    // wait for circe 0.6.X, where this bug will be fixed - https://github.com/travisbrown/circe/issues/304
    ignore("patcher modifies original object with a null value") {
      val original = LetterOpt(StreetAddress("my house"), StreetAddress("your house"), Option("hi there"))
      val modifier = encode(obj())
      val modifyLetter = patcher[LetterOpt](modifier)
      modifyLetter(original) shouldBe LetterOpt(StreetAddress("my house"), StreetAddress("your house"), None)
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](decode[Letter](Circe.JsonFormat.obj()))
    }

    it("body spec decodes content") {
      (Body(bodySpec[Letter]()) <-- Circe.ResponseBuilder.Ok(encode(aLetter)).build()) shouldBe aLetter
    }

    it("patch body can be used to modify an existing case class object") {
      val letterWithNoMessage = LetterOpt(StreetAddress("my house"), StreetAddress("your house"), None)
      val modifiedMessage = encode(obj("message" -> string("hi there")))
      val modifiedLetterWithMessage = LetterOpt(StreetAddress("my house"), StreetAddress("your house"), Some("hi there"))

      val patch = patchBody[LetterOpt]("path to body", modifiedLetterWithMessage) <-- Circe.ResponseBuilder.Ok(modifiedMessage).build()

      patch(letterWithNoMessage) shouldBe modifiedLetterWithMessage
    }

    it("response spec has correct code") {
      Circe.responseSpec[Letter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[Letter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }
  override val expectedJson: String = """{"string":"hello","object":{"field1":"aString"},"int":10,"long":2,"double":1.2,"decimal":1.2,"bigInt":12344,"bool":true,"null":null,"array":["world",true]}"""
}
