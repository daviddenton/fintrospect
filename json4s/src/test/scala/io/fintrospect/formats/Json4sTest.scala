package io.fintrospect.formats

import com.twitter.finagle.http.{Request, Status}
import io.fintrospect.parameters.{Body, BodySpec, Query}
import org.json4s.{JValue, MappingException}

import scala.language.reflectiveCalls


case class Json4sStreetAddress(address: String)

case class Json4sLetter(to: Json4sStreetAddress, from: Json4sStreetAddress, message: String)


abstract class Json4sFiltersSpec[D](json4sLibrary: Json4sLibrary[D]) extends AutoFiltersSpec(json4sLibrary.Filters) {

  import json4sLibrary.JsonFormat._

  override def toString(l: Letter): String = compact(encode(l))

  override def fromString(s: String): Letter = decode[Letter](parse(s))

  override def bodySpec: BodySpec[Letter] = json4sLibrary.bodySpec[Letter]()

  private def mf[T](implicit mf: Manifest[T]) = mf

  override def toOut() = json4sLibrary.Filters.tToToOut[Letter]
}

class Json4sNativeFiltersTest extends Json4sFiltersSpec(Json4s)

class Json4sJacksonFiltersTest extends Json4sFiltersSpec(Json4sJackson)

class Json4sNativeDoubleModeFiltersTest extends Json4sFiltersSpec(Json4sDoubleMode)

class Json4sJacksonDoubleModeFiltersTest extends Json4sFiltersSpec(Json4sJacksonDoubleMode)

abstract class RoundtripEncodeDecodeSpec[T](jsonLibrary: Json4sLibrary[T]) extends JsonFormatSpec(jsonLibrary) {

  val aLetter = Json4sLetter(Json4sStreetAddress("my house"), Json4sStreetAddress("your house"), "hi there")

  import jsonLibrary.JsonFormat._

  describe(format.getClass.getSimpleName) {
    it("roundtrips to JSON and back") {
      decode[Json4sLetter](encode(aLetter)) shouldBe aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[MappingException](decode[Json4sLetter](format.obj()))
    }
  }
}

class Json4sNativeEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s) {
  it("body spec decodes content") {
    (Body(Json4s.bodySpec[Json4sLetter]()) <-- Json4s.ResponseBuilder.Ok(Json4s.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4s.parameterSpec[Json4sLetter]("name"))
    (param <-- Request("?name=" + Json4s.JsonFormat.compact(Json4s.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4s.responseSpec[Json4sLetter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
  }
}

class Json4sNativeJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s)

class Json4sNativeDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4sDoubleMode) {
  it("body spec decodes content") {
    (Body(Json4sDoubleMode.bodySpec[Json4sLetter]()) <-- Json4sDoubleMode.ResponseBuilder.Ok(Json4sDoubleMode.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4sDoubleMode.parameterSpec[Json4sLetter]("name"))
    (param <-- Request("?name=" + Json4sDoubleMode.JsonFormat.compact(Json4sDoubleMode.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4sDoubleMode.responseSpec[Json4sLetter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
  }
}

class Json4sNativeDoubleJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4sDoubleMode)

class Json4sJacksonEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4sJackson) {
  it("body spec decodes content") {
    (Body(Json4sJackson.bodySpec[Json4sLetter]()) <-- Json4sDoubleMode.ResponseBuilder.Ok(Json4sJackson.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4sJackson.parameterSpec[Json4sLetter]("name"))
    (param <-- Request("?name=" + Json4sJackson.JsonFormat.compact(Json4sJackson.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4sJackson.responseSpec[Json4sLetter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
  }

}

class Json4sJacksonJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4sJackson)

class Json4sJacksonDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4sJacksonDoubleMode) {
  it("body spec decodes content") {
    (Body(Json4sJacksonDoubleMode.bodySpec[Json4sLetter]()) <-- Json4sJacksonDoubleMode.ResponseBuilder.Ok(Json4sJacksonDoubleMode.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4sJacksonDoubleMode.parameterSpec[Json4sLetter]("name"))
    (param <-- Request("?name=" + Json4sJacksonDoubleMode.JsonFormat.compact(Json4sJacksonDoubleMode.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4sJacksonDoubleMode.responseSpec[Json4sLetter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
  }

}

class Json4sJacksonDoubleJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4sJacksonDoubleMode)

