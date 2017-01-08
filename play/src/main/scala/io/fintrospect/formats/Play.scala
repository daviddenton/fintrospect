package io.fintrospect.formats

import java.math.BigInteger

import com.twitter.finagle.http.Status
import io.fintrospect.ResponseSpec
import io.fintrospect.formats.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.formats.Play.JsonFormat.encode
import io.fintrospect.parameters.{Body, BodySpec, ParameterSpec, UniBody}
import play.api.libs.json.{Json, Reads, _}

/**
  * Play JSON support (application/json content type)
  */
object Play extends JsonLibrary[JsValue, JsValue] {

  /**
    * Auto-marshalling Service wrappers that can be used to create Services which take and return domain objects
    * instead of HTTP responses
    */
  object Auto extends Auto[JsValue](ResponseBuilder) {
    implicit def tToBody[T](implicit r: Reads[T], w: Writes[T]): UniBody[T] = Body(Play.bodySpec[T]()(r, w))

    implicit def tToJsValue[T](implicit db: Writes[T]): Transform[T, JsValue] = (t: T) => JsonFormat.encode[T](t)
  }

  object JsonFormat extends JsonFormat[JsValue, JsValue] {

    override def parse(in: String): JsValue = Json.parse(in)

    override def pretty(node: JsValue): String = Json.prettyPrint(node)

    override def compact(node: JsValue): String = Json.stringify(node)

    override def obj(fields: Iterable[Field]): JsValue = JsObject(fields.map(f => field(f._1, f._2)).toSeq)

    override def array(elements: Iterable[JsValue]) = JsArray(elements.toSeq)

    override def string(value: String) = JsString(value)

    override def number(value: Int) = JsNumber(value)

    override def number(value: Double) = JsNumber(value)

    override def number(value: BigDecimal) = JsNumber(value.bigDecimal)

    override def number(value: Long) = JsNumber(value)

    override def number(value: BigInteger) = JsNumber(value.intValue())

    override def boolean(value: Boolean) = JsBoolean(value)

    override def nullNode() = JsNull

    private def field(name: String, value: JsValue) = name -> value

    def encode[T](in: T)(implicit writes: Writes[T]) = writes.writes(in)

    def decode[T](in: JsValue)(implicit reads: Reads[T]) = reads.reads(in).asOpt.getOrElse(throw new InvalidJsonForDecoding)
  }

  /**
    * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
    */
  def parameterSpec[R](name: String, description: Option[String] = None)(implicit reads: Reads[R], writes: Writes[R]) =
    ParameterSpec.json(name, description.orNull, Play).map(s => JsonFormat.decode[R](s), (u: R) => encode(u))

  /**
    * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
    */
  def responseSpec[R](statusAndDescription: (Status, String), example: R)
                     (implicit reads: Reads[R], writes: Writes[R]) =
    ResponseSpec.json(statusAndDescription, encode(example), Play)

  /**
    * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
    */
  def bodySpec[R](description: String = null)(implicit reads: Reads[R], writes: Writes[R]): BodySpec[R] =
    BodySpec.json(description, Play).map(j => JsonFormat.decode[R](j), (u: R) => encode(u))
}
