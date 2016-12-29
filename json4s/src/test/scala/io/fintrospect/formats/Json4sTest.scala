package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.parameters.{Body, Query}
import org.json4s.MappingException
import org.scalatest.{FunSpec, Matchers}

import scala.language.reflectiveCalls


case class Json4sStreetAddress(address: String)

case class Json4sLetter(to: Json4sStreetAddress, from: Json4sStreetAddress, message: String)

abstract class Json4sFiltersSpec(json4sLibrary: Json4sLibrary[_], filters: Json4sFilters[_], jsonFormat: Json4sFormat[_]) extends FunSpec with Matchers {

  import io.fintrospect.formats.Json4s.ResponseBuilder._
  import jsonFormat._

  describe("filters") {
    val aLetter = Json4sLetter(Json4sStreetAddress("my house"), Json4sStreetAddress("your house"), "hi there")

    val request = Request()
    request.contentString = jsonFormat.compact(jsonFormat.encode(aLetter))

    describe("AutoInOut") {
      it("returns Ok") {
        val svc = filters.AutoInOut(Service.mk { in: Json4sLetter => Future.value(in) }, Status.Created)

        val response = result(svc(request))
        response.status shouldBe Status.Created
        decode[Json4sLetter](parse(response.contentString)) shouldBe aLetter
      }
    }

    describe("AutoInOptionalOut") {
      it("returns Ok when present") {
        val svc = filters.AutoInOptionalOut(Service.mk[Json4sLetter, Option[Json4sLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(request))
        response.status shouldBe Status.Ok
        decode[Json4sLetter](parse(response.contentString)) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = filters.AutoInOptionalOut(Service.mk[Json4sLetter, Option[Json4sLetter]] { in => Future.value(None) })
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

    describe("AutoIn") {
      val svc = filters.AutoIn(Body(json4sLibrary.bodySpec[Json4sLetter]())).andThen(Service.mk { in: Json4sLetter => Ok(jsonFormat.encode(in)) })
      it("takes the object from the request") {
        jsonFormat.decode[Json4sLetter](jsonFormat.parse(result(svc(request)).contentString)) shouldBe aLetter
      }

      it("rejects illegal content with a BadRequest") {
        val request = Request()
        request.contentString = "not xml"
        Await.result(svc(request)).status shouldBe Status.BadRequest
      }
    }

    describe("AutoOut") {
      it("takes the object from the request") {
        val svc = filters.AutoOut[Json4sLetter, Json4sLetter](Status.Created).andThen(Service.mk { in: Json4sLetter => Future.value(in) })
        val response = result(svc(aLetter))
        response.status shouldBe Status.Created
        decode[Json4sLetter](parse(response.contentString)) shouldBe aLetter
      }
    }

    describe("AutoOptionalOut") {
      it("returns Ok when present") {
        val svc = filters.AutoOptionalOut[Json4sLetter, Json4sLetter](Status.Created).andThen(Service.mk[Json4sLetter, Option[Json4sLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(aLetter))
        response.status shouldBe Status.Created
        decode[Json4sLetter](parse(response.contentString)) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = filters.AutoOptionalOut[Json4sLetter, Json4sLetter](Status.Created).andThen(Service.mk[Json4sLetter, Option[Json4sLetter]] { in => Future.value(None) })
        result(svc(aLetter)).status shouldBe Status.NotFound
      }
    }
  }
}

class Json4sNativeFiltersTest extends Json4sFiltersSpec(Json4s, Json4s.Filters, Json4s.JsonFormat)

class Json4sJacksonFiltersTest extends Json4sFiltersSpec(Json4sJackson, Json4sJackson.Filters, Json4sJackson.JsonFormat)

class Json4sNativeDoubleModeFiltersTest extends Json4sFiltersSpec(Json4sDoubleMode, Json4sDoubleMode.Filters, Json4sDoubleMode.JsonFormat)

class Json4sJacksonDoubleModeFiltersTest extends Json4sFiltersSpec(Json4sJacksonDoubleMode, Json4sJacksonDoubleMode.Filters, Json4sJacksonDoubleMode.JsonFormat)

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

