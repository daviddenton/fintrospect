package io.fintrospect.formats.json

import io.fintrospect.util.json.Json4s
import io.fintrospect.util.json.Json4s.Json4sFormat

case class StreetAddress(address: String)

case class Letter(to: StreetAddress, from: StreetAddress, message: String)

class RoundtripEncodeDecodeSpec[T](format: Json4sFormat[T]) extends JsonFormatSpec[T](format) {

  describe(format.getClass.getSimpleName) {
    val aLetter = Letter(StreetAddress("my house"), StreetAddress("your house"), "hi there")
    it("roundtrips to json and back") {
      format.decode[Letter](format.encode(aLetter)) shouldEqual aLetter
    }
  }
}

class JsonNativeEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.Native.JsonFormat)

class Json4NativeDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.NativeDoubleMode.JsonFormat)

class Json4JacksonEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.Jackson.JsonFormat)

class Json4JacksonDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.JacksonDoubleMode.JsonFormat)
