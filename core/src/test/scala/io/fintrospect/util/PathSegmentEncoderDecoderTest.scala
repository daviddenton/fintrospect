package io.fintrospect.util

import io.fintrospect.util.PathSegmentEncoderDecoder.{decode, encode}
import org.scalatest.{FunSpec, Matchers}

class PathSegmentEncoderDecoderTest extends FunSpec with Matchers {

  describe("encode/decode") {
    it("roundtrips") {
      val inputString = " :@-._~!$&'()*+,;="
      decode(encode(inputString)) shouldBe inputString
    }

    it("does not url encode reserved characters") {
      encode(":@-._~!$&'()*+,;=") shouldEqual ":@-._~!$&'()*+,;="
    }

    it("handles spaces and forward slashes gracefully") {
      encode("a path/+piece") shouldEqual "a%20path%2F+piece"
    }
  }
}
