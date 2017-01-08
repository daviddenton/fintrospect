package io.fintrospect.formats

import java.math.BigInteger

import com.twitter.finagle.http.Status
import io.fintrospect.ResponseSpec
import io.fintrospect.parameters.{Body, BodySpec, ParameterSpec, UniBody}
import org.json4s.Extraction.decompose
import org.json4s.native.Document
import org.json4s.{Formats, JValue, JsonMethods, NoTypeHints, Serialization, _}

class Json4sFormat[+T](jsonMethods: JsonMethods[T],
                       val serialization: Serialization,
                       useBigDecimalForDouble: Boolean) extends JsonFormat[JValue, JValue] {

  override def pretty(in: JValue): String = jsonMethods.pretty(jsonMethods.render(in))

  override def parse(in: String): JValue = jsonMethods.parse(in, useBigDecimalForDouble)

  override def compact(in: JValue): String = jsonMethods.compact(jsonMethods.render(in))

  override def obj(fields: Iterable[Field]): JValue = JObject(fields.toList)

  override def string(value: String): JValue = JString(value)

  override def array(elements: Iterable[JValue]): JValue = JArray(elements.toList)

  override def boolean(value: Boolean): JValue = JBool(value)

  override def number(value: Int): JValue = JInt(value)

  override def number(value: Double) = JDouble(value)

  override def number(value: BigDecimal): JValue = JDecimal(value)

  override def number(value: Long): JValue = JInt(value)

  override def number(value: BigInteger): JValue = JInt(value)

  override def nullNode(): JValue = JNull

  def encode[R](in: R, formats: Formats = serialization.formats(NoTypeHints)): JValue = decompose(in)(formats)

  def decode[R](in: JValue,
                formats: Formats = serialization.formats(NoTypeHints))
               (implicit mf: Manifest[R]): R = in.extract[R](formats, mf)
}

abstract class Json4sLibrary[D] extends JsonLibrary[JValue, JValue] {

  val JsonFormat: Json4sFormat[D]

  import JsonFormat._

  object Auto extends Auto(ResponseBuilder) {
    implicit def tToBody[T](implicit mf: Manifest[T]): UniBody[T] = Body(bodySpec[T]())

    implicit def tToJValue[T]: (T) => JValue = (t: T) => JsonFormat.encode[T](t)
  }

  /**
    * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
    */
  def bodySpec[R](description: String = null, formats: Formats = serialization.formats(NoTypeHints))
                 (implicit mf: Manifest[R]) =
    BodySpec.json(description, this).map(j => decode[R](j, formats)(mf), (u: R) => encode(u))

  /**
    * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
    */
  def responseSpec[R](statusAndDescription: (Status, String), example: R, formats: Formats = serialization.formats(NoTypeHints))
                     (implicit mf: Manifest[R]) =
    ResponseSpec.json(statusAndDescription, encode(example), this)

  /**
    * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
    */
  def parameterSpec[R](name: String, description: Option[String] = None, formats: Formats = serialization.formats(NoTypeHints))
                      (implicit mf: Manifest[R]) =
    ParameterSpec.json(name, description.orNull, this).map(j => decode[R](j, formats)(mf), (u: R) => encode(u))

}

/**
  * Native Json4S support (application/json content type) - uses BigDecimal for decimal
  */
object Json4s extends Json4sLibrary[Document] {
  object JsonFormat extends Json4sFormat(org.json4s.native.JsonMethods, org.json4s.native.Serialization, true)
}

/**
  * Native Json4S support (application/json content type) - uses Doubles for decimal
  */
object Json4sDoubleMode extends Json4sLibrary[Document] {

  object JsonFormat extends Json4sFormat(org.json4s.native.JsonMethods, org.json4s.native.Serialization, false)
}

/**
  * Jackson Json4S support (application/json content type) - uses BigDecimal for decimal
  */
object Json4sJackson extends Json4sLibrary[JValue] {

  object JsonFormat extends Json4sFormat(org.json4s.jackson.JsonMethods, org.json4s.jackson.Serialization, true)
}

/**
  * Jackson Json4S support (application/json content type) - uses Doubles for decimal
  */
object Json4sJacksonDoubleMode extends Json4sLibrary[JValue] {

  object JsonFormat extends Json4sFormat(org.json4s.jackson.JsonMethods, org.json4s.jackson.Serialization, false)
}
