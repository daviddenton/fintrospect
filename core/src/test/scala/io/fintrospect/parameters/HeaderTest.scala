package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Message, Request, Response}
import io.fintrospect.RequestBuilder
import io.fintrospect.util.ExtractionError.{Invalid, Missing}
import io.fintrospect.util.{Extracted, ExtractionFailed}
import org.scalatest._

class HeaderTest extends FunSpec with Matchers {

  private val paramName = "name"

  describe("required") {
    describe("singular") {
      val param = Header.required.localDate(paramName)

      it("validate value from field") {
        param.extract(messageWithHeaderValueOf(Option("2015-02-04"))) shouldBe Extracted(Some(LocalDate.of(2015, 2, 4)))
        param <-- messageWithHeaderValueOf(Option("2015-02-04")) shouldBe LocalDate.of(2015, 2, 4)
      }

      it("fails to retrieve invalid value") {
        param.extract(messageWithHeaderValueOf(Option("notValid"))) shouldBe ExtractionFailed(Invalid(param))
      }

      it("does not retrieve non existent value") {
        param.extract(messageWithHeaderValueOf(None)) shouldBe ExtractionFailed(Missing(param))
      }

      it("can rebind valid value") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123")
        val bindings = Header.required.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap("field") shouldBe "123"
      }
    }

    describe("multi") {

      it("retrieves value from field") {
        val param = Header.required.multi.localDate(paramName)
        param.extract(messageWithValueOf("2015-02-04", "2015-02-05")) shouldBe Extracted(Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        param <-- messageWithValueOf("2015-02-04", "2015-02-05") shouldBe Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))
      }

      it("fails to retrieve invalid value") {
        val param = Header.required.*.long(paramName)
        param.extract(messageWithValueOf("qwe", "notValid")) shouldBe ExtractionFailed(Invalid(param))
      }

      it("does not retrieve non existent value") {
        val param = Header.required.*.zonedDateTime(paramName)
        param.extract(messageWithValueOf()) shouldBe ExtractionFailed(Missing(param))
      }

      it("can rebind valid value") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123").add("field", "456")
        val bindings = Header.required.*.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap.getAll("field") shouldBe Seq("123", "456")
      }
    }

  }

  describe("optional") {
    describe("singular") {
      val param = Header.optional.localDate(paramName)

      it("validate value from field") {
        param.extract(messageWithHeaderValueOf(Option("2015-02-04"))) shouldBe Extracted(Some(LocalDate.of(2015, 2, 4)))
        param <-- messageWithHeaderValueOf(Option("2015-02-04")) shouldBe Option(LocalDate.of(2015, 2, 4))
      }

      it("fails to retrieve invalid value") {
        param.extract(messageWithHeaderValueOf(Option("notValid"))) shouldBe ExtractionFailed(Invalid(param))
      }

      it("does not retrieve non existent value") {
        param.extract(messageWithHeaderValueOf(None)) shouldBe Extracted(None)
        param <-- Request() shouldBe None
      }

      it("can rebind valid value") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123")
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap("field") shouldBe "123"
      }

      it("does not rebind missing value") {
        val inRequest = Request()
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap.getAll("field") shouldBe Nil
      }
    }

    describe("multi") {
      val param = Header.optional.multi.localDate(paramName)

      it("retrieves value from field") {
        param.extract(messageWithValueOf("2015-02-04", "2015-02-05")) shouldBe Extracted(Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        param <-- messageWithValueOf("2015-02-04", "2015-02-05") shouldBe Option(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5)))
      }

      it("fails to retrieve invalid value") {
        param.extract(messageWithValueOf("2015-02-04", "notValid")) shouldBe ExtractionFailed(Invalid(param))
      }

      it("does not retrieve non existent value") {
        param.extract(messageWithValueOf()) shouldBe Extracted(None)
        param <-- Request() shouldBe None
      }

      it("can rebind valid value") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123").add("field", "456")
        val bindings = Header.optional.multi.int("field") rebind inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap.getAll("field") shouldBe Seq("123", "456")
      }

      it("doesn't rebind missing value") {
        val inRequest = Request("?")
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap.getAll("field") shouldBe Nil
      }
    }
  }

  private def messageWithHeaderValueOf(value: Iterable[String]): Message = {
    val request = Response()
    value.foreach(v => request.headerMap.add(paramName, v))
    request
  }

  private def messageWithValueOf(value: String*): Message = {
    val request = Response()
    value.foreach(v => request.headerMap.add(paramName, v))
    request
  }
}
