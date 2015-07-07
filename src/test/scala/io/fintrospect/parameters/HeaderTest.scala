package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Request
import org.jboss.netty.handler.codec.http.HttpMethod
import org.scalatest._

class HeaderTest extends FunSpec with ShouldMatchers {

  private val paramName = "name"

  describe("required") {
    val param = Header.required.localDate(paramName)

    it("validate value from field") {
      param.validate(requestWithValueOf(Option("2015-02-04"))) shouldEqual Right(Option(LocalDate.of(2015, 2, 4)))
      param <-- requestWithValueOf(Option("2015-02-04")) shouldEqual LocalDate.of(2015, 2, 4)
    }

    it("fails to retrieve invalid value") {
      param.validate(requestWithValueOf(Option("notValid"))) shouldEqual Left(param)
    }

    it("does not retrieve non existent value") {
      param.validate(requestWithValueOf(None)) shouldEqual Left(param)
    }

    it("can rebind valid value") {
      val inRequest = Request()
      inRequest.headers().add("field", "123")
      val bindings = Header.required.int("field") <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
      outRequest.headers().get("field") shouldEqual "123"
    }
  }

  describe("optional") {
    val param = Header.optional.localDate(paramName)

    it("validate value from field") {
      param.validate(requestWithValueOf(Option("2015-02-04"))) shouldEqual Right(Option(LocalDate.of(2015, 2, 4)))
      param <-- requestWithValueOf(Option("2015-02-04")) shouldEqual Option(LocalDate.of(2015, 2, 4))
    }

    it("fails to retrieve invalid value") {
      param.validate(requestWithValueOf(Option("notValid"))) shouldEqual Left(param)
    }

    it("does not retrieve non existent value") {
      param.validate(requestWithValueOf(None)) shouldEqual Right(None)
      param <-- Request() shouldEqual None
    }

    it("can rebind valid value") {
      val inRequest = Request()
      inRequest.headers().add("field", "123")
      val bindings = Header.optional.int("field") <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
      outRequest.headers().get("field") shouldEqual "123"
    }

    it("does not rebind missing value") {
      val inRequest = Request()
      val bindings = Header.optional.int("field") <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
      outRequest.headers().get("field") shouldEqual null
    }
  }

  private def requestWithValueOf(value: Option[String]) = {
    val request = Request()
    value.foreach(v => request.headers().add(paramName, v))
    request
  }
}
