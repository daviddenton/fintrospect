package io.fintrospect.formats

import com.twitter.io.Buf
import io.fintrospect.formats.MsgPack.Format._
import org.scalatest.{FunSpec, Matchers}
import org.velvia.InvalidMsgPackDataException

class MsgPackFormatTest extends FunSpec with Matchers {

  describe("roundtrips") {
    val aLetter = Letter(StreetAddress("my house"), StreetAddress("your house"), "hi there")

      it("roundtrips to JSON and back") {
        decode[Letter](encode(aLetter)) shouldBe aLetter
      }

      it("invalid extracted Msg throws up") {
        intercept[InvalidMsgPackDataException](decode[Letter](Buf.Empty))
      }
  }
}
