package io.fintrospect.util.json

import io.fintrospect.util.json.Json4sFormat.NumberMode._
import org.json4s.{JField, JValue}

object Json4sResponseBuilder {
  def native(numberMode: NumberMode = UseBigDecimal) = new JsonResponseBuilder[JValue, JValue, JField](Json4sFormat.native(numberMode))
  def jackson(numberMode: NumberMode = UseBigDecimal) = new JsonResponseBuilder[JValue, JValue, JField](Json4sFormat.jackson(numberMode))
}
