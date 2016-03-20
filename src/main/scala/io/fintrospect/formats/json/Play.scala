package io.fintrospect.formats.json

import java.math.BigInteger

import com.twitter.finagle.http.Status
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.ResponseSpec
import io.fintrospect.formats.json.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Body, BodySpec, ObjectParamType, ParameterSpec}
import play.api.libs.json.{Json, _}

/**
 * Play JSON support (application/json content type)
 */
object Play extends JsonLibrary[JsValue, JsValue] {

  object JsonFormat extends JsonFormat[JsValue, JsValue] {

    override def parse(in: String): JsValue = Json.parse(in)

    override def pretty(node: JsValue): String = Json.prettyPrint(node)

    override def compact(node: JsValue): String = Json.stringify(node)

    override def obj(fields: Iterable[Field]): JsValue = JsObject(fields.map(f => field(f._1, f._2)).toSeq)

    override def obj(fields: Field*): JsValue = JsObject(fields.map(f => field(f._1, f._2)))

    override def array(elements: Iterable[JsValue]) = JsArray(elements.toSeq)

    override def array(elements: JsValue*) = JsArray(elements.toSeq)

    override def string(value: String) = JsString(value)

    override def number(value: Int) = JsNumber(value)

    override def number(value: BigDecimal) = JsNumber(value.bigDecimal)

    override def number(value: Long) = JsNumber(value)

    override def number(value: BigInteger) = JsNumber(value.intValue())

    override def boolean(value: Boolean) = JsBoolean(value)

    override def nullNode() = JsNull

    private def field(name: String, value: JsValue) = name -> value

    def encode[T](in: T)(implicit writes: Writes[T]) = writes.writes(in)

    def decode[T](in: JsValue)(implicit reads: Reads[T]) = reads.reads(in).asOpt.getOrElse(throw new InvalidJsonForDecoding)

    /**
      * Convenience method for creating Body that just use straight JSON encoding/decoding logic
      */
    def body[R](description: Option[String] = None, example: R = null)
               (implicit reads: Reads[R], writes: Writes[R]) = Body(bodySpec[R](description)(reads, writes), example, ObjectParamType)

    /**
     * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
     */
    def bodySpec[R](description: Option[String] = None)(implicit reads: Reads[R], writes: Writes[R]) =
      BodySpec[R](description, APPLICATION_JSON, s => decode[R](parse(s))(reads), (u: R) => compact(encode(u)(writes)))

    /**
      * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
      */
    def responseSpec[R](statusAndDescription: (Status, String), example: R)
                       (implicit reads: Reads[R], writes: Writes[R]) =
      ResponseSpec.json(statusAndDescription, encode(example)(writes), this)

    /**
     * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
     */
    def parameterSpec[R](name: String, description: Option[String] = None)(implicit reads: Reads[R], writes: Writes[R]) =
      ParameterSpec[R](name, description, ObjectParamType, s => decode[R](parse(s))(reads), (u: R) => compact(encode(u)(writes)))
  }

}
