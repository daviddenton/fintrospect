package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.io.{Buf, Bufs}
import com.twitter.util.Future
import io.fintrospect.formats.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.formats.Play.Auto._
import io.fintrospect.formats.Play.JsonFormat._
import io.fintrospect.formats.Play._
import io.fintrospect.parameters.{Body, BodySpec, Query}
import play.api.libs.json._



object helpers {
  implicit val SAWrites = new Writes[StreetAddress] {
    override def writes(in: StreetAddress) = JsObject(Seq("address" -> JsString(in.address)))
  }
  implicit val SAReads = new Reads[StreetAddress] {
    override def reads(in: JsValue) = JsSuccess(StreetAddress(
      (in \ "address").as[String]
    ))
  }

  implicit val Writes = Json.writes[Letter]
  implicit val Reads = Json.reads[Letter]
}


class PlayAutoTest extends AutoSpec(Play.Auto) {

  import helpers._

  describe("API") {
    it("can find implicits") {
      Play.Auto.InOut[Letter, Letter](Service.mk { in: Letter => Future(in) })
    }
  }

  override def toBuf(l: Letter) = Bufs.utf8Buf(compact(Play.JsonFormat.encode(l)(Writes)))

  override def fromBuf(s: Buf): Letter = decode[Letter](parse(Bufs.asUtf8String(s)))(Reads)

  override def bodySpec: BodySpec[Letter] = Play.bodySpec[Letter]()(helpers.Reads, helpers.Writes)

  override def transform() = Play.Auto.tToJsValue[Letter](Writes)
}

class PlayJsonResponseBuilderTest extends JsonResponseBuilderSpec(Play)

class PlayJsonFormatTest extends JsonFormatSpec(Play) {

  describe("Play.JsonFormat") {
    val aLetter = Letter(StreetAddress("my house"), StreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = Play.JsonFormat.encode(aLetter)(helpers.Writes)
      Play.JsonFormat.decode[Letter](encoded)(helpers.Reads) shouldBe aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](Play.JsonFormat.decode[Letter](Play.JsonFormat.obj())(helpers.Reads))
    }

    it("body spec decodes content") {
      (Body.of(bodySpec[Letter]()(helpers.Reads, helpers.Writes)) <-- Play.ResponseBuilder.Ok(encode(aLetter)(helpers.Writes)).build()) shouldBe aLetter
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[Letter]()(helpers.Reads, helpers.Writes), "name")
      (param <-- Request("?name=" + encode(aLetter)(helpers.Writes))) shouldBe aLetter
    }
  }

}
