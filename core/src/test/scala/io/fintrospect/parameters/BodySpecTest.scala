package io.fintrospect.parameters

import io.fintrospect.ContentTypes
import io.fintrospect.formats.json.Argo.JsonFormat.{obj, string}
import org.scalatest._

import scala.util.{Success, Try}

class BodySpecTest extends FunSpec with Matchers {

  val paramName = "name"

  describe("json") {
    val expected = """{"name":"value"}"""
    val asJson = obj("name" -> string("value"))

    it("retrieves a valid value") {
      Try(BodySpec.json().deserialize(expected)) shouldEqual Success(asJson)
    }

    it("does not retrieve an invalid value") {
      Try(BodySpec.json().deserialize("notJson")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(BodySpec.json().deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      BodySpec.json().serialize(asJson) shouldEqual expected
    }
  }

  describe("xml") {
    val expected = <field>value</field>

    it("retrieves a valid value") {
      Try(BodySpec.xml().deserialize(expected.toString())) shouldEqual Success(expected)
    }

    it("does not retrieve an invalid value") {
      Try(BodySpec.xml().deserialize("notXml")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(BodySpec.xml().deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      BodySpec.xml().serialize(expected) shouldEqual """<field>value</field>"""
    }
  }

  describe("custom") {

    val bodySpec = BodySpec[MyCustomType](None, ContentTypes.TEXT_PLAIN, s => MyCustomType(s.toInt), ct => ct.value.toString)

    it("retrieves a valid value") {
      Try(bodySpec.deserialize("123")) shouldEqual Success(MyCustomType(123))
    }

    it("does not retrieve an invalid value") {
      Try(bodySpec.deserialize("notAnInt")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(bodySpec.deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      bodySpec.serialize(MyCustomType(123)) shouldEqual "123"
    }
  }

  describe("Map to another BodySpec") {
    case class IClass(value: String)

    it("can map with just read") {
      BodySpec.string(None).map(IClass).deserialize("123") shouldBe IClass("123")
    }

    it("can map with read and show") {
      BodySpec.string(None).map[IClass](IClass, (i:IClass) => i.value + i.value).serialize(IClass("100")) shouldBe "100100"
    }
  }
}
