package io.fintrospect.formats

import com.twitter.finagle.http.Request
import io.fintrospect.formats.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.formats.Play.JsonFormat._
import io.fintrospect.formats.Play._
import io.fintrospect.parameters.{Body, BodySpec, Query}
import play.api.libs.json._

import scala.language.reflectiveCalls

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


class PlayFilterTest extends AutoFiltersSpec(Play.Filters) {

  override def toString(l: Letter): String = compact(Play.JsonFormat.encode(l)(helpers.Writes))

  override def fromString(s: String): Letter = decode[Letter](parse(s))(helpers.Reads)

  override def bodySpec: BodySpec[Letter] = Play.bodySpec[Letter]()(helpers.Reads, helpers.Writes)

  override def toOut() = Play.Filters.tToToOut[Letter](helpers.Writes)
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
      (Body(bodySpec[Letter]()(helpers.Reads, helpers.Writes)) <-- Play.ResponseBuilder.Ok(encode(aLetter)(helpers.Writes)).build()) shouldBe aLetter
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[Letter]("name")(helpers.Reads, helpers.Writes))
      (param <-- Request("?name=" + encode(aLetter)(helpers.Writes))) shouldBe aLetter
    }
  }

}
