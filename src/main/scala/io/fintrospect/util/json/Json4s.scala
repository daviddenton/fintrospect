package io.fintrospect.util.json

import java.math.BigInteger

import io.fintrospect.util.json.NumberMode.NumberMode
import org.json4s._


object Json4s {

  private abstract class AbstractJson4sFormat(numberMode: NumberMode) extends JsonFormat[JValue, JValue, JField] {

    import org.json4s.JsonDSL._
    import org.json4s._

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

  }

  def native(numberMode: NumberMode = NumberMode.UseBigDecimal): JsonLibrary[JValue, JValue, JField] = {
    new JsonLibrary[JValue, JValue, JField] {
      override val JsonFormat = new AbstractJson4sFormat(numberMode) {

        import org.json4s._

        def parse(in: String): JValue = org.json4s.native.JsonMethods.parse(in, numberMode == NumberMode.UseBigDecimal)

        def compact(in: JValue): String = org.json4s.native.JsonMethods.compact(org.json4s.native.JsonMethods.render(in))

        def pretty(in: JValue): String = org.json4s.native.JsonMethods.pretty(org.json4s.native.JsonMethods.render(in))
      }
    }
  }

  def jackson(numberMode: NumberMode = NumberMode.UseBigDecimal): JsonLibrary[JValue, JValue, JField] = {
    new JsonLibrary[JValue, JValue, JField] {
      override val JsonFormat = new AbstractJson4sFormat(numberMode) {

        import org.json4s._

        def parse(in: String): JValue = org.json4s.jackson.JsonMethods.parse(in, numberMode == NumberMode.UseBigDecimal)

        def compact(in: JValue): String = org.json4s.jackson.JsonMethods.compact(org.json4s.jackson.JsonMethods.render(in))

        def pretty(in: JValue): String = org.json4s.jackson.JsonMethods.pretty(org.json4s.jackson.JsonMethods.render(in))
      }
    }

  }
}
