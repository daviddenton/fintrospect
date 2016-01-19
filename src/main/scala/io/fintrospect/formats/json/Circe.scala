package io.fintrospect.formats.json

import java.math.BigInteger

import io.circe.Json.{JNumber, JString}
import io.fintrospect.ContentTypes._
import io.fintrospect.formats.json.JsonFormat.{InvalidJson, InvalidJsonForDecoding}
import io.fintrospect.parameters.{BodySpec, ObjectParamType, ParameterSpec}
import io.circe._, io.circe.generic.auto._, io.circe.jawn._, io.circe.syntax._
import cats.data.Xor
import io.circe._, io.circe.generic.auto._, io.circe.parse._, io.circe.syntax._
import io.circe._
import io.circe.generic.auto._
import io.circe.parse._
import io.circe.syntax._

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

  }
}
