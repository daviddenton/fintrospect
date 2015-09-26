package io.fintrospect.util.json

import io.fintrospect.util.json.Json4sFormat.NumberMode.NumberMode
import org.json4s.JValue

object Json4sFormat {

  object NumberMode extends Enumeration {
    type NumberMode = Value
    val UseBigDecimal, DoubleMode = Value
  }

  def native(numberMode: NumberMode = NumberMode.UseBigDecimal) = new JsonFormat[JValue, JValue] {

    import org.json4s.JsonDSL._
    import org.json4s._

    def parse(in: String): JValue = org.json4s.native.JsonMethods.parse(in, numberMode == NumberMode.UseBigDecimal)

    def compact(in: JValue): String = org.json4s.native.JsonMethods.compact(org.json4s.native.JsonMethods.render(in))

    def pretty(in: JValue): String = org.json4s.native.JsonMethods.pretty(org.json4s.native.JsonMethods.render(in))

    override def obj(fields: Iterable[Field]): JValue = fields
  }

  def jackson(numberMode: NumberMode = NumberMode.UseBigDecimal) = new JsonFormat[JValue, JValue] {
    import org.json4s.JsonDSL._
    import org.json4s._

    def parse(in: String): JValue = org.json4s.jackson.JsonMethods.parse(in, numberMode == NumberMode.UseBigDecimal)

    def compact(in: JValue): String = org.json4s.jackson.JsonMethods.compact(org.json4s.jackson.JsonMethods.render(in))

    def pretty(in: JValue): String = org.json4s.jackson.JsonMethods.pretty(org.json4s.jackson.JsonMethods.render(in))

    override def obj(fields: Iterable[Field]): JValue = fields
  }
}
