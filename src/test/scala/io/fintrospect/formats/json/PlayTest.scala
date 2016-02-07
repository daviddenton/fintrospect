package io.fintrospect.formats.json

import com.twitter.finagle.http.Request
import io.fintrospect.formats.json.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.formats.json.Play.JsonFormat._
import io.fintrospect.parameters.{Body, Query}
import play.api.libs.json._

case class PlayStreetAddress(address: String)

object PlayStreetAddress {
  implicit val Writes = new Writes[PlayStreetAddress] {
    override def writes(in: PlayStreetAddress) = JsObject(Seq("address" -> JsString(in.address)))
  }
  implicit val Reads = new Reads[PlayStreetAddress] {
    override def reads(in: JsValue) = JsSuccess(PlayStreetAddress(
      (in \ "address").as[String]
    ))
  }
}

case class PlayLetter(to: PlayStreetAddress, from: PlayStreetAddress, message: String)

object PlayLetter {

  implicit val R = PlayStreetAddress.Reads
  implicit val W = PlayStreetAddress.Writes

  implicit val Writes = Json.writes[PlayLetter]
  implicit val Reads = Json.reads[PlayLetter]
}

class PlayJsonResponseBuilderTest extends JsonResponseBuilderSpec(Play)

class PlayJsonFormatTest extends JsonFormatSpec(Play.JsonFormat) {

  describe("Play.JsonFormat") {
    val aLetter = PlayLetter(PlayStreetAddress("my house"), PlayStreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = Play.JsonFormat.encode(aLetter)(PlayLetter.Writes)
      Play.JsonFormat.decode[PlayLetter](encoded)(PlayLetter.Reads) shouldEqual aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](Play.JsonFormat.decode[PlayLetter](Play.JsonFormat.obj()))
    }

    it("body spec decodes content") {
      (Body(bodySpec[PlayLetter]()) <-- Play.ResponseBuilder.OK(encode(aLetter)).build()) shouldBe aLetter
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[PlayLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }

}
