package io.fintrospect.formats.json

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{Created, Ok}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.formats.json.Json4s.{Json4sFilters, Json4sFormat}
import io.fintrospect.parameters.{Body, Query}
import org.json4s.MappingException
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.language.reflectiveCalls

case class Json4sStreetAddress(address: String)

case class Json4sLetter(to: Json4sStreetAddress, from: Json4sStreetAddress, message: String)

abstract class Json4sFiltersSpec(filters: Json4sFilters[_], jsonFormat: Json4sFormat[_]) extends FunSpec with ShouldMatchers {

  import jsonFormat._

  describe("filters") {
    val aLetter = Json4sLetter(Json4sStreetAddress("my house"), Json4sStreetAddress("your house"), "hi there")

    val request = Request()
    request.contentString = jsonFormat.compact(jsonFormat.encode(aLetter))

    describe("AutoInOut") {
      it("returns Ok") {
        val svc = filters.AutoInOut(Service.mk { in: Json4sLetter => Future.value(in) }, Created)

        val response = result(svc(request))
        response.status shouldEqual Created
        decode[Json4sLetter](parse(response.contentString)) shouldEqual aLetter
      }
    }

    describe("AutoInOptionalOut") {
      it("returns Ok when present") {
        val svc = filters.AutoInOptionalOut(Service.mk[Json4sLetter, Option[Json4sLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(request))
        response.status shouldEqual Ok
        decode[Json4sLetter](parse(response.contentString)) shouldEqual aLetter
      }

      it("returns NotFound when missing present") {
        val svc = filters.AutoInOptionalOut(Service.mk[Json4sLetter, Option[Json4sLetter]] { in => Future.value(None) })
        result(svc(request)).status shouldEqual Status.NotFound
      }
    }

    describe("AutoIn") {
      it("takes the object from the request") {
        val svc = filters.AutoIn(jsonFormat.body[Json4sLetter]()).andThen(Service.mk { in: Json4sLetter => Future.value(in) })
        result(svc(request)) shouldEqual aLetter
      }
    }

    describe("AutoOut") {
      it("takes the object from the request") {
        val svc = filters.AutoOut[Json4sLetter, Json4sLetter](Created).andThen(Service.mk { in: Json4sLetter => Future.value(in) })
        val response = result(svc(aLetter))
        response.status shouldEqual Created
        decode[Json4sLetter](parse(response.contentString)) shouldEqual aLetter
      }
    }

    describe("AutoOptionalOut") {
      it("returns Ok when present") {
        val svc = filters.AutoOptionalOut[Json4sLetter, Json4sLetter](Created).andThen(Service.mk[Json4sLetter, Option[Json4sLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(aLetter))
        response.status shouldEqual Created
        decode[Json4sLetter](parse(response.contentString)) shouldEqual aLetter
      }

      it("returns NotFound when missing present") {
        val svc = filters.AutoOptionalOut[Json4sLetter, Json4sLetter](Created).andThen(Service.mk[Json4sLetter, Option[Json4sLetter]] { in => Future.value(None) })
        result(svc(aLetter)).status shouldEqual Status.NotFound
      }
    }
  }
}

class Json4sNativeFiltersTest extends Json4sFiltersSpec(Json4s.Native.Filters, Json4s.Native.JsonFormat)
class Json4sJacksonFiltersTest extends Json4sFiltersSpec(Json4s.Jackson.Filters, Json4s.Jackson.JsonFormat)
class Json4sNativeDoubleModeFiltersTest extends Json4sFiltersSpec(Json4s.NativeDoubleMode.Filters, Json4s.NativeDoubleMode.JsonFormat)
class Json4sJacksonDoubleModeFiltersTest extends Json4sFiltersSpec(Json4s.JacksonDoubleMode.Filters, Json4s.JacksonDoubleMode.JsonFormat)

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

