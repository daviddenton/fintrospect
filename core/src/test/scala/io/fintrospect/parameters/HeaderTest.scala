package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Message, Request, Response}
import io.fintrospect.parameters.ExtractionError.{Invalid, Missing}
import org.scalatest._

class HeaderTest extends FunSpec with ShouldMatchers {

  private val paramName = "name"

  describe("required") {
    describe("singular") {
      val param = Header.required.localDate(paramName)

      it("validate value from field") {
        param.extract(messageWithHeaderValueOf(Option("2015-02-04"))) shouldEqual Extracted(Some(LocalDate.of(2015, 2, 4)))
        param <-- messageWithHeaderValueOf(Option("2015-02-04")) shouldEqual LocalDate.of(2015, 2, 4)
      }

      it("fails to retrieve invalid value") {
        param.extract(messageWithHeaderValueOf(Option("notValid"))) shouldEqual ExtractionFailed(Invalid(param.name))
      }

      it("does not retrieve non existent value") {
        param.extract(messageWithHeaderValueOf(None)) shouldEqual ExtractionFailed(Missing(param.name))
      }

      it("can rebind valid value") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123")
        val bindings = Header.required.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap("field") shouldEqual "123"
      }
    }

    describe("multi") {

      it("retrieves value from field") {
        val param = Header.required.multi.localDate(paramName)
        param.extract(messageWithValueOf("2015-02-04", "2015-02-05")) shouldEqual Extracted(Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        param <-- messageWithValueOf("2015-02-04", "2015-02-05") shouldEqual Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))
      }

      it("fails to retrieve invalid value") {
        val param = Header.required.*.long(paramName)
        param.extract(messageWithValueOf("qwe", "notValid")) shouldEqual ExtractionFailed(Invalid(param.name))
      }

      it("does not retrieve non existent value") {
        val param = Header.required.*.zonedDateTime(paramName)
        param.extract(messageWithValueOf()) shouldEqual ExtractionFailed(Missing(param.name))
      }

      it("can rebind valid value") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123").add("field", "456")
        val bindings = Header.required.*.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap.getAll("field") shouldEqual Seq("123", "456")
      }
    }

  }

  describe("optional") {
    describe("singular") {
      val param = Header.optional.localDate(paramName)

      it("validate value from field") {
        param.extract(messageWithHeaderValueOf(Option("2015-02-04"))) shouldEqual Extracted(Some(LocalDate.of(2015, 2, 4)))
        param <-- messageWithHeaderValueOf(Option("2015-02-04")) shouldEqual Option(LocalDate.of(2015, 2, 4))
      }

      it("fails to retrieve invalid value") {
        param.extract(messageWithHeaderValueOf(Option("notValid"))) shouldEqual ExtractionFailed(Invalid(param.name))
      }

      it("does not retrieve non existent value") {
        param.extract(messageWithHeaderValueOf(None)) shouldEqual Extracted(None)
        param <-- Request() shouldEqual None
      }

      it("can rebind valid value") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123")
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap("field") shouldEqual "123"
      }

      it("does not rebind missing value") {
        val inRequest = Request()
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap.getAll("field") shouldEqual Nil
      }
    }

    describe("multi") {
      val param = Header.optional.multi.localDate(paramName)

      it("retrieves value from field") {
        param.extract(messageWithValueOf("2015-02-04", "2015-02-05")) shouldEqual Extracted(Some(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        param <-- messageWithValueOf("2015-02-04", "2015-02-05") shouldEqual Option(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5)))
      }

      it("fails to retrieve invalid value") {
        param.extract(messageWithValueOf("2015-02-04", "notValid")) shouldEqual ExtractionFailed(Invalid(param.name))
      }

      it("does not retrieve non existent value") {
        param.extract(messageWithValueOf()) shouldEqual Extracted(None)
        param <-- Request() shouldEqual None
      }

      it("can rebind valid value") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123").add("field", "456")
        val bindings = Header.optional.multi.int("field") rebind inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap.getAll("field") shouldEqual Seq("123", "456")
      }

      it("doesn't rebind missing value") {
        val inRequest = Request("?")
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap.getAll("field") shouldEqual Nil
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
