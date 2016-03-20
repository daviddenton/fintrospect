package io.fintrospect.formats.json

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.Ok
import io.fintrospect.formats.json.Json4s.Json4sFormat
import io.fintrospect.parameters.{Body, Query}
import org.json4s.MappingException

import scala.language.reflectiveCalls

case class Json4sStreetAddress(address: String)

case class Json4sLetter(to: Json4sStreetAddress, from: Json4sStreetAddress, message: String)

abstract class RoundtripEncodeDecodeSpec[T](format: Json4sFormat[T]) extends JsonFormatSpec(format) {

  val aLetter = Json4sLetter(Json4sStreetAddress("my house"), Json4sStreetAddress("your house"), "hi there")

  describe(format.getClass.getSimpleName) {
    it("roundtrips to JSON and back") {
      format.decode[Json4sLetter](format.encode(aLetter)) shouldEqual aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[MappingException](format.decode[Json4sLetter](format.obj()))
    }
  }
}

class Json4sNativeEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.Native.JsonFormat) {
  it("body spec decodes content") {
    (Body(Json4s.Native.JsonFormat.bodySpec[Json4sLetter]()) <-- Json4s.Native.ResponseBuilder.OK(Json4s.Native.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4s.Native.JsonFormat.parameterSpec[Json4sLetter]("name"))
    (param <-- Request("?name=" + Json4s.Native.JsonFormat.compact(Json4s.Native.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4s.Native.JsonFormat.responseSpec[Json4sLetter](Ok -> "ok", aLetter).status shouldBe Ok
  }
}

class Json4sNativeJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s.Native)

class Json4sNativeDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.NativeDoubleMode.JsonFormat) {
  it("body spec decodes content") {
    (Body(Json4s.NativeDoubleMode.JsonFormat.bodySpec[Json4sLetter]()) <-- Json4s.NativeDoubleMode.ResponseBuilder.OK(Json4s.NativeDoubleMode.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4s.NativeDoubleMode.JsonFormat.parameterSpec[Json4sLetter]("name"))
    (param <-- Request("?name=" + Json4s.NativeDoubleMode.JsonFormat.compact(Json4s.NativeDoubleMode.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4s.NativeDoubleMode.JsonFormat.responseSpec[Json4sLetter](Ok -> "ok", aLetter).status shouldBe Ok
  }
}

class Json4sNativeDoubleJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s.NativeDoubleMode)

class Json4sJacksonEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.Jackson.JsonFormat) {
  it("body spec decodes content") {
    (Body(Json4s.Jackson.JsonFormat.bodySpec[Json4sLetter]()) <-- Json4s.NativeDoubleMode.ResponseBuilder.OK(Json4s.Jackson.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4s.Jackson.JsonFormat.parameterSpec[Json4sLetter]("name"))
    (param <-- Request("?name=" + Json4s.Jackson.JsonFormat.compact(Json4s.Jackson.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4s.Jackson.JsonFormat.responseSpec[Json4sLetter](Ok -> "ok", aLetter).status shouldBe Ok
  }

}

class Json4sJacksonJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s.Jackson)

class Json4sJacksonDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.JacksonDoubleMode.JsonFormat) {
  it("body spec decodes content") {
    (Body(Json4s.JacksonDoubleMode.JsonFormat.bodySpec[Json4sLetter]()) <-- Json4s.JacksonDoubleMode.ResponseBuilder.OK(Json4s.JacksonDoubleMode.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4s.JacksonDoubleMode.JsonFormat.parameterSpec[Json4sLetter]("name"))
    (param <-- Request("?name=" + Json4s.JacksonDoubleMode.JsonFormat.compact(Json4s.JacksonDoubleMode.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4s.JacksonDoubleMode.JsonFormat.responseSpec[Json4sLetter](Ok -> "ok", aLetter).status shouldBe Ok
  }

}

class Json4sJacksonDoubleJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s.JacksonDoubleMode)

