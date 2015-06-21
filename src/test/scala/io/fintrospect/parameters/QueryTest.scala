package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Request
import org.scalatest._

class QueryTest extends FunSpec with ShouldMatchers {

  private val paramName = "name"

  describe("required") {
    val param = Query.required.localDate(paramName)

    it("retrieves value from field") {
      param.validate(requestWithValueOf(Some("2015-02-04"))) shouldEqual Right(Some(LocalDate.of(2015, 2, 4)))
      param.from(requestWithValueOf(Some("2015-02-04"))) shouldEqual LocalDate.of(2015, 2, 4)
    }

    it("fails to retrieve invalid value") {
      param.validate(requestWithValueOf(Some("notValid"))) shouldEqual Left(param)
    }

    it("does not retrieve non existent value") {
      param.validate(requestWithValueOf(None)) shouldEqual Left(param)
    }

  }

  describe("optional") {
    val param = Query.optional.localDate(paramName)

    it("retrieves value from field") {
      param.validate(requestWithValueOf(Some("2015-02-04"))) shouldEqual Right(Some(LocalDate.of(2015, 2, 4)))
      param.from(requestWithValueOf(Some("2015-02-04"))) shouldEqual Some(LocalDate.of(2015, 2, 4))
    }

    it("fails to retrieve invalid value") {
      param.validate(requestWithValueOf(Some("notValid"))) shouldEqual Left(param)
    }

    it("does not retrieve non existent value") {
      param.validate(requestWithValueOf(None)) shouldEqual Right(None)
      param.from(Request()) shouldEqual None
    }
  }

  private def requestWithValueOf(value: Option[String]) = {
    value.map(s => Request(paramName -> s)).getOrElse(Request())
  }
}
