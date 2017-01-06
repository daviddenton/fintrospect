package io.fintrospect.formats

import java.math.BigInteger

import argonaut.Argonaut._
import argonaut.{DecodeJson, EncodeJson, Json}
import com.twitter.finagle.http.Status
import io.fintrospect.ResponseSpec
import io.fintrospect.formats.JsonFormat.{InvalidJson, InvalidJsonForDecoding}
import io.fintrospect.parameters.{Body, BodySpec, ParameterSpec, UniBody}

/**
  * Argonaut JSON support (application/json content type)
  */
object Argonaut extends JsonLibrary[Json, Json] {

  /**
    * Auto-marshalling Service wrappers that can be used to create Services which take and return domain objects
    * instead of HTTP responses
    */
  object Auto extends Auto[Json](ResponseBuilder) {

    implicit def tToBody[T](implicit e: EncodeJson[T], d: DecodeJson[T]): UniBody[T] = Body(bodySpec[T]())

    implicit def tToJson[T](implicit e: EncodeJson[T]): Transform[T, Json] = (t: T) => e(t)
  }


  object JsonFormat extends JsonFormat[Json, Json] {

    override def parse(in: String): Json = in.parseOption.getOrElse(throw new InvalidJson)

    override def pretty(node: Json): String = node.spaces2

    override def compact(node: Json): String = node.nospaces

    override def obj(fields: Iterable[Field]): Json = Json.obj(fields.map(f => (f._1, f._2)).toSeq: _*)

    override def array(elements: Iterable[Json]) = Json.array(elements.toSeq: _*)

    override def string(value: String) = jString(value)

    override def number(value: Int) = jNumber(value)

    override def number(value: BigDecimal) = jNumber(value.doubleValue())

    override def number(value: Long) = jNumber(value)

    override def number(value: BigInteger) = jNumber(value.intValue())

    override def boolean(value: Boolean) = jBool(value)

    override def nullNode() = jNull

    def encode[T](in: T)(implicit encodec: EncodeJson[T]) = encodec.encode(in)

    def decode[T](in: Json)(implicit decodec: DecodeJson[T]) = decodec.decodeJson(in).getOr(throw new InvalidJsonForDecoding)
  }

  /**
    * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
    */
  def bodySpec[R](description: Option[String] = None)(implicit encodec: EncodeJson[R], decodec: DecodeJson[R]) =
    BodySpec.json(description, this).map(j => JsonFormat.decode[R](j)(decodec), (u: R) => JsonFormat.encode(u)(encodec))

  /**
    * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
    */
  def responseSpec[R](statusAndDescription: (Status, String), example: R)
                     (implicit encodec: EncodeJson[R], decodec: DecodeJson[R]) =
    ResponseSpec.json(statusAndDescription, JsonFormat.encode(example)(encodec), this)

  /**
    * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
    */
  def parameterSpec[R](name: String, description: Option[String] = None)(implicit encodec: EncodeJson[R], decodec: DecodeJson[R]) =
    ParameterSpec.json(name, description.orNull, this).map(j => JsonFormat.decode[R](j)(decodec), (u: R) => JsonFormat.encode(u)(encodec))
}
