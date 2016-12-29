package io.fintrospect.formats

import com.twitter.finagle.http.Status
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import org.scalatest.{FunSpec, Matchers}

abstract class ResponseBuilderSpec[T](bldr: AbstractResponseBuilder[T]) extends FunSpec with Matchers {

  val message = "some text goes here"

  val customType: T
  val customTypeSerialized: String

  val customError: T
  val customErrorSerialized: String

  describe("Rendering") {
    it("builds non error with custom type") {
      statusAndContentFrom(bldr.Ok(customType)) shouldBe(Status.Ok, customTypeSerialized)
    }

    it("builds error with message - String") {
      statusAndContentFrom(bldr.NotFound(customError)) shouldBe(Status.NotFound, customErrorSerialized)
      statusAndContentFrom(bldr.NotFound(customError)) shouldBe(Status.NotFound, customErrorSerialized)
    }
  }

}
