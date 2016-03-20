package io.fintrospect.util

import io.fintrospect.util.PathSegmentEncoderDecoder.encode
import io.fintrospect.util.PathSegmentEncoderDecoder.decode
import org.scalatest.{FunSpec, ShouldMatchers}

class PathSegmentEncoderDecoder$Test extends FunSpec with ShouldMatchers{

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
