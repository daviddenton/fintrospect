package io.fintrospect.formats

import com.twitter.finagle.http.Status
import com.twitter.io.{Buf, Bufs}
import org.scalatest.{FunSpec, Matchers}

abstract class ResponseBuilderSpec[T](bldr: AbstractResponseBuilder[T]) extends FunSpec with Matchers {

  val message = "some text goes here"

  val customType: T
  val customTypeSerialized: Buf

  val customError: T
  val customErrorSerialized: Buf

  describe("Rendering") {
    it("builds non error with custom type") {
      val rsp = bldr.Ok(customType)
      (rsp.status, Bufs.asByteArrayBuf(rsp.content)) shouldBe(Status.Ok, Bufs.asByteArrayBuf(customTypeSerialized))
    }

    it("builds error with message - String") {
      val rsp = bldr.NotFound(customError)
      (rsp.status, Bufs.asByteArrayBuf(rsp.content)) shouldBe(Status.NotFound, Bufs.asByteArrayBuf(customErrorSerialized))
    }
  }

}
