package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Request
import org.scalatest._

import scala.util.Success

class QueryTest extends FunSpec with ShouldMatchers {

  private val paramName = "name"

  describe("required") {
    val param = Query.required.localDate(paramName)

    it("retrieves value from field") {
      param.attemptToParseFrom(requestWithValueOf(Some("2015-02-04"))) shouldEqual Some(Success(LocalDate.of(2015, 2, 4)))
      param.from(requestWithValueOf(Some("2015-02-04"))) shouldEqual LocalDate.of(2015, 2, 4)
    }

    it("fails to retrieve invalid value") {
      param.attemptToParseFrom(requestWithValueOf(Some("notValid"))).get.isFailure shouldEqual true
    }

    it("does not retrieve non existent value") {
      param.attemptToParseFrom(requestWithValueOf(None)) shouldEqual None
    }

  }

  describe("optional") {
    val param = Query.optional.localDate(paramName)

    it("retrieves value from field") {
      param.attemptToParseFrom(requestWithValueOf(Some("2015-02-04"))) shouldEqual Some(Success(LocalDate.of(2015, 2, 4)))
      param.from(requestWithValueOf(Some("2015-02-04"))) shouldEqual Some(LocalDate.of(2015, 2, 4))
    }

    it("fails to retrieve invalid value") {
      param.attemptToParseFrom(requestWithValueOf(Some("notValid"))).get.isFailure shouldEqual true
    }

    it("does not retrieve non existent value") {
      param.attemptToParseFrom(requestWithValueOf(None)) shouldEqual None
      param.from(Request()) shouldEqual None
    }
  }

  private def requestWithValueOf(value: Option[String]) = {
    value.map(s => Request(paramName -> s)).getOrElse(Request())
  }
}
