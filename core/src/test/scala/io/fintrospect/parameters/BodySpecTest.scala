package io.fintrospect.parameters

import com.twitter.finagle.http.Status
import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray.Shared.extract
import io.fintrospect.formats.Argo.JsonFormat.{obj, string}
import io.fintrospect.formats.Xml
import io.fintrospect.util.{Extracted, ExtractionFailed}
import io.fintrospect.{ContentType, ContentTypes}
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

  describe("binary") {
    val expected = Buf.ByteArray("test".getBytes: _*)

    it("retrieves a valid value") {
      Try(BodySpec.binary(ContentType("application/exe")).deserialize(expected)) shouldBe Success(expected)
    }

    it("does not retrieve an invalid value") {
      Try(BodySpec.binary(ContentType("application/exe")).deserialize(Buf.Empty)).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(BodySpec.binary(ContentType("application/exe")).deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      BodySpec.binary(ContentType("application/exe")).serialize(expected) shouldBe expected
    }
  }

  describe("string") {
    val bodySpec = BodySpec.string()

    it("does not retrieve an null value") {
      Try(bodySpec.deserialize(null)).isFailure shouldBe true
    }

    it("does not retrieve an empty value") {
      Try(bodySpec.deserialize(Buf.Utf8(""))).isFailure shouldBe true
    }

    it("can override validation so empty is OK") {
      Try(BodySpec.string(validation = StringValidations.EmptyIsValid).deserialize(Buf.Utf8(""))) shouldBe Success("")
    }
  }

  describe("custom") {
    val bodySpec = BodySpec[MyCustomType](ContentTypes.TEXT_PLAIN, StringParamType, b => MyCustomType(new String(extract(b)).toInt), ct => Buf.Utf8(ct.value.toString))

    it("retrieves a valid value") {
      Try(bodySpec.deserialize(Buf.Utf8("123"))) shouldBe Success(MyCustomType(123))
    }

    it("does not retrieve an invalid value") {
      Try(bodySpec.deserialize(Buf.Utf8("notAnInt"))).isFailure shouldBe true
    }

    it("does not retrieve an empty value") {
      Try(bodySpec.deserialize(Buf.Utf8(""))).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(bodySpec.deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      bodySpec.serialize(MyCustomType(123)) shouldBe Buf.Utf8("123")
    }

  }

  describe("extraction from a response") {
    it("when extracts response successfully") {
      val extractor = Body.xml().responseExtractor
      extractor <--? Xml.ResponseBuilder.Ok(<xml/>) shouldBe Extracted(Some(<xml/>))
    }

    it("default predicate is response is NOT NotFound, falls back to empty extraction") {
      val extractor = Body.xml().responseExtractor
      extractor <--? Xml.ResponseBuilder.NotFound("who knows") shouldBe Extracted(None)
    }

    it("when custom predicate fails, fall back to empty extraction") {
      val extractor = Body.xml().responseExtractor(_.status != Status.Ok)
      extractor <--? Xml.ResponseBuilder.Ok(<xml/>) shouldBe Extracted(None)
    }

    it("when extraction fails") {
      val extractor = Body.xml()

      extractor <--? Xml.ResponseBuilder.Ok("some nonsense") match {
        case ExtractionFailed(_) =>
        case _ => fail("extraction not as expected")
      }
    }
  }

  describe("Map to another BodySpec") {
    case class IClass(value: String)

    it("can map with just read") {
      BodySpec.string().map(IClass).deserialize(Buf.Utf8("123")) shouldBe IClass("123")
    }

    it("can map with read and show") {
      BodySpec.string().map[IClass](IClass, (i: IClass) => i.value + i.value).serialize(IClass("100")) shouldBe Buf.Utf8("100100")
    }
  }
}
