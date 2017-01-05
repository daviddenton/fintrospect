package io.fintrospect.formats

import org.scalatest.{FunSpec, Matchers}

class MsgPackMsgTest extends FunSpec with Matchers {

  describe("MsgPackMsg") {
    it("can round trip to bytes and back again") {
      val letter = Letter(StreetAddress("bob"), StreetAddress("jim"), "rita")
      MsgPackMsg(letter).as[Letter] shouldBe letter
    }
  }

}
