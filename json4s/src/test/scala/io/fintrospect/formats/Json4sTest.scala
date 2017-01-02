package io.fintrospect.formats

import com.twitter.finagle.http.{Request, Status}
import com.twitter.io.{Buf, Bufs}
import io.fintrospect.parameters.{Body, BodySpec, Query}
import org.json4s.MappingException

import scala.language.reflectiveCalls

abstract class Json4sFiltersSpec[D](json4sLibrary: Json4sLibrary[D]) extends AutoFiltersSpec(json4sLibrary.Auto) {

  import json4sLibrary.JsonFormat._

  override def toBuf(l: Letter) = Bufs.utf8Buf(compact(encode(l)))

  override def fromBuf(s: Buf): Letter = decode[Letter](parse(Bufs.asUtf8String(s)))

  override def bodySpec: BodySpec[Letter] = json4sLibrary.bodySpec[Letter]()

  override def transform() = json4sLibrary.Auto.tToToOut[Letter]
}

class Json4SNativeAutoTest extends Json4sFiltersSpec(Json4s)

class Json4SJacksonAutoTest extends Json4sFiltersSpec(Json4sJackson)

class Json4SNativeDoubleModeAutoTest extends Json4sFiltersSpec(Json4sDoubleMode)

class Json4SJacksonDoubleModeAutoTest extends Json4sFiltersSpec(Json4sJacksonDoubleMode)

abstract class RoundtripEncodeDecodeSpec[T](jsonLibrary: Json4sLibrary[T]) extends JsonFormatSpec(jsonLibrary) {

  val aLetter = Letter(StreetAddress("my house"), StreetAddress("your house"), "hi there")

  import jsonLibrary.JsonFormat._

  describe(format.getClass.getSimpleName) {
    it("roundtrips to JSON and back") {
      decode[Letter](encode(aLetter)) shouldBe aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[MappingException](decode[Letter](format.obj()))
    }
  }
}

class Json4sNativeEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s) {
  it("body spec decodes content") {
    (Body(Json4s.bodySpec[Letter]()) <-- Json4s.ResponseBuilder.Ok(Json4s.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4s.parameterSpec[Letter]("name"))
    (param <-- Request("?name=" + Json4s.JsonFormat.compact(Json4s.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4s.responseSpec[Letter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
  }
}

class Json4sNativeJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s)

class Json4sNativeDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4sDoubleMode) {
  it("body spec decodes content") {
    (Body(Json4sDoubleMode.bodySpec[Letter]()) <-- Json4sDoubleMode.ResponseBuilder.Ok(Json4sDoubleMode.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4sDoubleMode.parameterSpec[Letter]("name"))
    (param <-- Request("?name=" + Json4sDoubleMode.JsonFormat.compact(Json4sDoubleMode.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4sDoubleMode.responseSpec[Letter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
  }
}

class Json4sNativeDoubleJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4sDoubleMode)

class Json4sJacksonEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4sJackson) {
  it("body spec decodes content") {
    (Body(Json4sJackson.bodySpec[Letter]()) <-- Json4sDoubleMode.ResponseBuilder.Ok(Json4sJackson.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4sJackson.parameterSpec[Letter]("name"))
    (param <-- Request("?name=" + Json4sJackson.JsonFormat.compact(Json4sJackson.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4sJackson.responseSpec[Letter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
  }

}

class Json4sJacksonJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4sJackson)

class Json4sJacksonDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4sJacksonDoubleMode) {
  it("body spec decodes content") {
    (Body(Json4sJacksonDoubleMode.bodySpec[Letter]()) <-- Json4sJacksonDoubleMode.ResponseBuilder.Ok(Json4sJacksonDoubleMode.JsonFormat.encode(aLetter)).build()) shouldBe aLetter
  }

  it("param spec decodes content") {
    val param = Query.required(Json4sJacksonDoubleMode.parameterSpec[Letter]("name"))
    (param <-- Request("?name=" + Json4sJacksonDoubleMode.JsonFormat.compact(Json4sJacksonDoubleMode.JsonFormat.encode(aLetter)))) shouldBe aLetter
  }

  it("response spec has correct code") {
    Json4sJacksonDoubleMode.responseSpec[Letter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
  }

}

class Json4sJacksonDoubleJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4sJacksonDoubleMode)

