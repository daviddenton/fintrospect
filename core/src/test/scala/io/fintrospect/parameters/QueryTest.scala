package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import io.fintrospect.RequestBuilder
import io.fintrospect.util.ExtractionError.{Invalid, Missing}
import io.fintrospect.util.{Extracted, ExtractionFailed}
import org.scalatest._


class QueryTest extends FunSpec with Matchers {

  private val paramName = "name"

  describe("required") {
    describe("singular") {
      val param = Query.required.localDate(paramName)

      it("retrieves value from field") {
        param.extract(requestWithValueOf("2015-02-04")) shouldBe Extracted(Some(LocalDate.of(2015, 2, 4)))
        param <-- requestWithValueOf("2015-02-04") shouldBe LocalDate.of(2015, 2, 4)
      }

      it("fails to retrieve invalid value") {
        param.extract(requestWithValueOf("notValid")) shouldBe ExtractionFailed(Invalid(param))
      }

      it("does not retrieve non existent value") {
        param.extract(requestWithValueOf()) shouldBe ExtractionFailed(Missing(param))
      }

      it("can rebind valid value") {
        val inRequest = Request("?field=123")
        val bindings = Query.required.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldBe "/?field=123"
      }
    }

    describe("multi") {

      it("retrieves value from field") {
        val param = Query.required.multi.localDate(paramName)
        param.extract(requestWithValueOf("2015-02-04", "2015-02-05")) shouldBe Extracted(Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        param <-- requestWithValueOf("2015-02-04", "2015-02-05") shouldBe Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))
      }

      it("fails to retrieve invalid value") {
        val param = Query.required.*.long(paramName)
        param.extract(requestWithValueOf("qwe","notValid")) shouldBe ExtractionFailed(Invalid(param))
      }

      it("does not retrieve non existent value") {
        val param = Query.required.*.zonedDateTime(paramName)
        param.extract(requestWithValueOf()) shouldBe ExtractionFailed(Missing(param))
      }

      it("can rebind valid value") {
        val inRequest = Request("?field=123&field=456")
        val bindings = Query.required.*.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldBe "/?field=456&field=123"
      }
    }
  }

  describe("optional") {
    describe("singular") {

      it("retrieves value from field") {
        val param = Query.optional.localDate(paramName)
        param.extract(requestWithValueOf("2015-02-04")) shouldBe Extracted(Some(LocalDate.of(2015, 2, 4)))
        param <-- requestWithValueOf("2015-02-04") shouldBe Option(LocalDate.of(2015, 2, 4))
      }

      it("fails to retrieve invalid value") {
        val param = Query.optional.json(paramName)
        param.extract(requestWithValueOf("notValid")) shouldBe ExtractionFailed(Invalid(param))
      }

      it("does not retrieve non existent value") {
        val param = Query.optional.xml(paramName)
        param.extract(requestWithValueOf()) shouldBe Extracted(None)
        param <-- Request() shouldBe None
      }

      it("can rebind valid value") {
        val inRequest = Request("?field=123&field=456")
        val bindings = Query.optional.*.int("field") rebind inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldBe "/?field=456&field=123"
      }

      it("doesn't rebind missing value") {
        val inRequest = Request("?")
        val bindings = Query.optional.dateTime("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldBe "/"
      }
    }
    describe("multi") {
      val param = Query.optional.multi.localDate(paramName)

      it("retrieves value from field") {
        param.extract(requestWithValueOf("2015-02-04", "2015-02-05")) shouldBe Extracted(Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        param <-- requestWithValueOf("2015-02-04", "2015-02-05") shouldBe Option(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5)))
      }

      it("fails to retrieve invalid value") {
        param.extract(requestWithValueOf("2015-02-04", "notValid")) shouldBe ExtractionFailed(Invalid(param))
      }

      it("does not retrieve non existent value") {
        param.extract(requestWithValueOf()) shouldBe Extracted(None)
        param <-- Request() shouldBe None
      }

      it("can rebind valid value") {
        val inRequest = Request("?field=123&field=456")
        val bindings = Query.optional.multi.int("field") rebind inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldBe "/?field=456&field=123"
      }

      it("doesn't rebind missing value") {
        val inRequest = Request("?")
        val bindings = Query.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldBe "/"
      }
    }
  }

  private def requestWithValueOf(value: String*) = {
    Request(value.map(value => paramName -> value): _*)
  }
}
