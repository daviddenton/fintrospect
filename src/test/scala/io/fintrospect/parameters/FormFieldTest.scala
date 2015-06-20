package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.{MediaType, Request}
import org.jboss.netty.handler.codec.http.QueryStringEncoder
import org.scalatest._

import scala.util.Success

class FormFieldTest extends FunSpec with ShouldMatchers {

  private val paramName = "name"

  describe("required") {
    val param = FormField.required.localDate(paramName)

    it("retrieves value from form field") {
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
    val field = FormField.optional.localDate(paramName)

    it("retrieves value from form field") {
      field.attemptToParseFrom(requestWithValueOf(Some("2015-02-04"))) shouldEqual Some(Success(LocalDate.of(2015, 2, 4)))
      field.from(requestWithValueOf(Some("2015-02-04"))) shouldEqual Some(LocalDate.of(2015, 2, 4))
    }

    it("fails to retrieve invalid value") {
      field.attemptToParseFrom(requestWithValueOf(Some("notValid"))).get.isFailure shouldEqual true
    }

    it("does not retrieve non existent value") {
      field.attemptToParseFrom(requestWithValueOf(None)) shouldEqual None
      field.from(Request()) shouldEqual None
    }
  }

  private def requestWithValueOf(value: Option[String]) = {
    val request = Request()
    request.setContentType(MediaType.WwwForm)
    value.foreach { v =>
      request.setContentString(new QueryStringEncoder("") {
        {
          addParam(paramName, v)
        }
      }.toUri.getRawQuery)
    }
    request
  }
}
