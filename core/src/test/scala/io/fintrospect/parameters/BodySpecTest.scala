package io.fintrospect.parameters

import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray.Shared.extract
import io.fintrospect.ContentTypes
import io.fintrospect.formats.Argo.JsonFormat.{obj, string}
import org.scalatest._

import scala.util.{Success, Try}

class BodySpecTest extends FunSpec with Matchers {

  val paramName = "name"

  describe("json") {
    val expected = Buf.Utf8("""{"name":"value"}""")
    val asJson = obj("name" -> string("value"))

    it("retrieves a valid value") {
      Try(BodySpec.json().deserialize(expected)) shouldBe Success(asJson)
    }

    it("does not retrieve an invalid value") {
      Try(BodySpec.json().deserialize(Buf.Utf8("notJson"))).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(BodySpec.json().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      BodySpec.json().serialize(asJson) shouldBe expected
    }
  }

  describe("xml") {
    val expected = <field>value</field>

    it("retrieves a valid value") {
      Try(BodySpec.xml().deserialize(Buf.Utf8(expected.toString()))) shouldBe Success(expected)
    }

    it("does not retrieve an invalid value") {
      Try(BodySpec.xml().deserialize(Buf.Utf8("notXml"))).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(BodySpec.xml().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      BodySpec.xml().serialize(expected) shouldBe Buf.Utf8("""<field>value</field>""")
    }
  }

  describe("custom") {
    val bodySpec = BodySpec[MyCustomType](None, ContentTypes.TEXT_PLAIN, b => MyCustomType(new String(extract(b)).toInt), ct => Buf.Utf8(ct.value.toString))

    it("retrieves a valid value") {
      Try(bodySpec.deserialize(Buf.Utf8("123"))) shouldBe Success(MyCustomType(123))
    }

    it("does not retrieve an invalid value") {
      Try(bodySpec.deserialize(Buf.Utf8("notAnInt"))).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(bodySpec.deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      bodySpec.serialize(MyCustomType(123)) shouldBe Buf.Utf8("123")
    }
  }

  describe("Map to another BodySpec") {
    case class IClass(value: String)

    it("can map with just read") {
      BodySpec.string(None).map(IClass).deserialize(Buf.Utf8("123")) shouldBe IClass("123")
    }

    it("can map with read and show") {
      BodySpec.string(None).map[IClass](IClass, (i:IClass) => i.value + i.value).serialize(IClass("100")) shouldBe Buf.Utf8("100100")
    }
  }
}
