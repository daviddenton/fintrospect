package io.fintrospect.formats.json

import java.math.BigInteger

import com.twitter.finagle.http.Status
import io.circe._
import io.fintrospect.ContentTypes._
import io.fintrospect.ResponseSpec
import io.fintrospect.formats.json.JsonFormat.{InvalidJson, InvalidJsonForDecoding}
import io.fintrospect.parameters.{BodySpec, ObjectParamType, ParameterSpec}

/**
  * Circe JSON support (application/json content type)
  */
object Circe extends JsonLibrary[Json, Json] {

  object JsonFormat extends JsonFormat[Json, Json] {

    override def parse(in: String): Json = io.circe.jawn.parse(in).getOrElse(throw new InvalidJson)

    override def pretty(node: Json): String = node.spaces2

    override def compact(node: Json): String = node.noSpaces

    override def obj(fields: Iterable[Field]): Json = Json.obj(fields.map(f => (f._1, f._2)).toSeq: _*)

    override def obj(fields: Field*): Json = Json.obj(fields.map(f => (f._1, f._2)): _*)

    override def array(elements: Iterable[Json]) = Json.array(elements.toSeq: _*)

    override def array(elements: Json*) = array(elements)

    override def string(value: String) = Json.string(value)

    override def number(value: Int) = Json.numberOrNull(value)

    override def number(value: BigDecimal) = Json.bigDecimal(value.doubleValue())

    override def number(value: Long) = Json.long(value)

    override def number(value: BigInteger) = Json.long(value.intValue())

    override def boolean(value: Boolean) = Json.bool(value)

    override def nullNode() = Json.empty

    def encode[T](in: T)(implicit e: Encoder[T]) = e(in)

    def decode[T](in: Json)(implicit d: Decoder[T]) = d.decodeJson(in).getOrElse(throw new InvalidJsonForDecoding)

    /**
      * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
      */
    def bodySpec[R](description: Option[String] = None)(implicit encodec: Encoder[R], decodec: Decoder[R]) =
      BodySpec[R](description, APPLICATION_JSON, s => decode[R](parse(s))(decodec), (u: R) => compact(encode(u)(encodec)))

    /**
      * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
      */
    def responseSpec[R](statusAndDescription: (Status, String), example: R)
                       (implicit encodec: Encoder[R], decodec: Decoder[R]) =
      ResponseSpec.json(statusAndDescription, encode(example)(encodec), this)

    /**
      * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
      */
    def parameterSpec[R](name: String, description: Option[String] = None)(implicit encodec: Encoder[R], decodec: Decoder[R]) =
      ParameterSpec[R](name, description, ObjectParamType, s => decode[R](parse(s))(decodec), (u: R) => compact(encode(u)(encodec)))
  }

}
