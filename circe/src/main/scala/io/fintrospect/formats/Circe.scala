package io.fintrospect.formats

import java.math.BigInteger

import com.twitter.finagle.http.Status
import io.circe.{Decoder, Encoder, Json}
import io.fintrospect.ResponseSpec
import io.fintrospect.formats.Circe.JsonFormat.encode
import io.fintrospect.formats.JsonFormat.{InvalidJson, InvalidJsonForDecoding}
import io.fintrospect.parameters._

/**
  * Circe JSON support (application/json content type)
  */
object Circe extends JsonLibrary[Json, Json] {

  /**
    * Auto-marshalling Service wrappers that can be used to create Services which take and return domain objects
    * instead of HTTP responses
    */
  object Auto extends Auto[Json](ResponseBuilder) {

    implicit def tToBody[T](implicit e: Encoder[T], d: Decoder[T]): Body[T] = Body.of(bodySpec[T]())

    implicit def tToJson[T](implicit e: Encoder[T]): (T => Json) = (t: T) => e(t)
  }

  object JsonFormat extends JsonFormat[Json, Json] {

    override def parse(in: String): Json = io.circe.jawn.parse(in) match {
      case Right(r) => r
      case Left(e) => throw new InvalidJson
    }

    override def pretty(node: Json): String = node.spaces2

    override def compact(node: Json): String = node.noSpaces

    override def obj(fields: Iterable[Field]): Json = Json.obj(fields.map(f => (f._1, f._2)).toSeq: _*)

    override def array(elements: Iterable[Json]) = Json.arr(elements.toSeq: _*)

    override def string(value: String) = Json.fromString(value)

    override def number(value: Int) = Json.fromInt(value)

    override def number(value: Double) = Json.fromDoubleOrString(value)

    override def number(value: BigDecimal) = Json.fromBigDecimal(value.doubleValue)

    override def number(value: Long) = Json.fromLong(value)

    override def number(value: BigInteger) = Json.fromBigInt(value)

    override def boolean(value: Boolean) = Json.fromBoolean(value)

    override def nullNode() = Json.Null

    def encode[T](in: T)(implicit e: Encoder[T]) = e(in)

    def decode[T](in: Json)(implicit d: Decoder[T]) = d.decodeJson(in) match {
      case Right(r) => r
      case Left(_) => throw new InvalidJsonForDecoding
    }
  }

  /**
    * Function that will modify a given case class with the fields from a incoming JSON object.
    * Useful for PATCH/PUT requests, where only modified fields are sent to the server.
    */
  def patcher[T](in: Json)(implicit d: Decoder[T => T]) = JsonFormat.decode[T => T](in)

  /**
    * A Body that provides a function that will modify a given case class with the fields from a incoming JSON object.
    * Useful for PATCH/PUT requests, where only fields to be modified are sent to the server. Note that this Body only
    * supports inbound messages.
    */
  def patchBody[R](description: String = null, example: R = null)
                  (implicit e: Encoder[R], d: Decoder[R => R]): Body[R => R] = Body.of[R => R](
    BodySpec.json(this).map(j => JsonFormat.decode[R => R](j),
      (u: R => R) => encode(u(example))), description, Option(example).map(_ => (r: R) => example).orNull)

  /**
    * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
    */
  def bodySpec[R]()(implicit e: Encoder[R], d: Decoder[R]) =
    BodySpec.json(this).map(j => JsonFormat.decode[R](j), (u: R) => encode(u))

  /**
    * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
    */
  def responseSpec[R](statusAndDescription: (Status, String), example: R)
                     (implicit e: Encoder[R], d: Decoder[R]) =
    ResponseSpec.json(statusAndDescription, encode(example), this)

  /**
    * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
    */
  def parameterSpec[R]()(implicit e: Encoder[R], d: Decoder[R]) =
    ParameterSpec.json(this).map(j => JsonFormat.decode[R](j), (u: R) => encode(u))

}
