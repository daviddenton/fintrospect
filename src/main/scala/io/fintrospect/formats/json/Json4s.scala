package io.fintrospect.formats.json

import java.math.BigInteger

import org.json4s.Extraction.decompose
import org.json4s._

/**
 * Json4S support
 */
object Json4s {

  class Json4sFormat[T](jsonMethods: JsonMethods[T],
                        serialization: Serialization,
                        useBigDecimalForDouble: Boolean) extends JsonFormat[JValue, JValue] {

    import org.json4s.JsonDSL._
    import org.json4s._

    override def pretty(in: JValue): String = jsonMethods.pretty(jsonMethods.render(in))

    override def parse(in: String): JValue = jsonMethods.parse(in, useBigDecimalForDouble)

    override def compact(in: JValue): String = jsonMethods.compact(jsonMethods.render(in))

    override def obj(fields: Iterable[Field]): JValue = fields

    override def obj(fields: (String, JValue)*): JValue = fields

    override def string(value: String): JValue = JString(value)

    override def array(elements: JValue*): JValue = JArray(elements.toList)

    override def array(elements: Iterable[JValue]): JValue = JArray(elements.toList)

    override def boolean(value: Boolean): JValue = JBool(value)

    override def number(value: Int): JValue = JInt(value)

    override def number(value: BigDecimal): JValue = JDecimal(value)

    override def number(value: Long): JValue = JInt(value)

    override def number(value: BigInteger): JValue = JInt(value)

    override def nullNode(): JValue = JNull

    def encode(in: AnyRef, formats: Formats = serialization.formats(NoTypeHints)): JValue = decompose(in)(formats)

    def decode[R](in: JValue,
                  formats: Formats = serialization.formats(NoTypeHints))
                 (implicit mf: scala.reflect.Manifest[R]): R = {
      in.extract[R](formats, mf)
    }
  }

  /**
   * Native Json4S support - uses BigDecimal for decimal
   */
  object Native extends JsonLibrary[JValue, JValue] {

    val JsonFormat = new Json4sFormat(org.json4s.native.JsonMethods, org.json4s.native.Serialization, true)

  }

  /**
   * Native Json4S support - uses Doubles for decimal
   */
  object NativeDoubleMode extends JsonLibrary[JValue, JValue] {

    val JsonFormat = new Json4sFormat(org.json4s.native.JsonMethods, org.json4s.native.Serialization, false)

  }

  /**
   * Jackson Json4S support - uses BigDecimal for decimal
   */
  object Jackson extends JsonLibrary[JValue, JValue] {

    val JsonFormat = new Json4sFormat(org.json4s.jackson.JsonMethods, org.json4s.jackson.Serialization, true)

  }

  /**
   * Jackson Json4S support - uses Doubles for decimal
   */
  object JacksonDoubleMode extends JsonLibrary[JValue, JValue] {

    val JsonFormat = new Json4sFormat(org.json4s.jackson.JsonMethods, org.json4s.jackson.Serialization, false)

  }

}
