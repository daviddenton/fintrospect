package io.fintrospect.formats.json

import java.math.BigInteger

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import io.circe._
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.ResponseSpec
import io.fintrospect.formats.json.JsonFormat.{InvalidJson, InvalidJsonForDecoding}
import io.fintrospect.parameters.{Body, BodySpec, ObjectParamType, ParameterSpec}

/**
  * Circe JSON support (application/json content type)
  */
object Circe extends JsonLibrary[Json, Json] {

  /**
    * Auto-marshalling filters which can be used to create Services which take and return domain objects
    * instead of HTTP responses
    */
  object Filters extends AbstractFilters(Circe) {

    import Circe.ResponseBuilder.implicits._

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOut[BODY, OUT](svc: Service[BODY, OUT], successStatus: Status = Ok)
                            (implicit db: Decoder[BODY], eb: Encoder[BODY], e: Encoder[OUT], example: BODY = null)
    : Service[Request, Response] = {
      val body = Body[BODY](Circe.JsonFormat.bodySpec[BODY](None)(eb, db), example, ObjectParamType)
      AutoIn[BODY, Response](body).andThen(AutoOut[BODY, OUT](successStatus)(e)).andThen(svc)
    }

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which may return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoInOptionalOut[BODY, OUT](svc: Service[BODY, Option[OUT]], successStatus: Status = Ok)
                                    (implicit db: Decoder[BODY], eb: Encoder[BODY], e: Encoder[OUT], example: BODY = null)
    : Service[Request, Response] = {
      val body = Body[BODY](Circe.JsonFormat.bodySpec[BODY](None)(eb, db), example, ObjectParamType)
      AutoIn[BODY, Response](body).andThen(AutoOptionalOut[BODY, OUT](successStatus)(e)).andThen(svc)
    }

    /**
      * Filter to provide auto-marshalling of output case class instances for HTTP scenarios where an object is returned.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoOut[IN, OUT](successStatus: Status = Ok)
                        (implicit e: Encoder[OUT]): Filter[IN, Response, IN, OUT]
    = _AutoOut((t: OUT) => successStatus(Circe.JsonFormat.encode(t)(e)))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP scenarios where an object may not be returned
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoOptionalOut[IN, OUT](successStatus: Status = Ok)
                                (implicit e: Encoder[OUT]): Filter[IN, Response, IN, Option[OUT]]
    = _AutoOptionalOut((t: OUT) => successStatus(Circe.JsonFormat.encode(t)(e)))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP POST scenarios
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOutFilter[BODY, OUT]
    (implicit successStatus: Status = Ok, db: Decoder[BODY], eb: Encoder[BODY], e: Encoder[OUT], example: BODY = null)
    : Filter[Request, Response, BODY, OUT] = {
      val body = Body[BODY](Circe.JsonFormat.bodySpec[BODY](None)(eb, db), example, ObjectParamType)
      AutoIn[BODY, Response](body).andThen(AutoOut[BODY, OUT](successStatus)(e))
    }
  }

  object JsonFormat extends JsonFormat[Json, Json] {

    override def parse(in: String): Json = io.circe.jawn.parse(in).getOrElse(throw new InvalidJson)

    override def pretty(node: Json): String = node.spaces2

    override def compact(node: Json): String = node.noSpaces

    override def obj(fields: Iterable[Field]): Json = Json.obj(fields.map(f => (f._1, f._2)).toSeq: _*)

    override def obj(fields: Field*): Json = Json.obj(fields.map(f => (f._1, f._2)): _*)

    override def array(elements: Iterable[Json]) = Json.arr(elements.toSeq: _*)

    override def array(elements: Json*) = array(elements)

    override def string(value: String) = Json.fromString(value)

    override def number(value: Int) = Json.fromDoubleOrNull(value)

    override def number(value: BigDecimal) = Json.fromBigDecimal(value.doubleValue())

    override def number(value: Long) = Json.fromLong(value)

    override def number(value: BigInteger) = Json.fromLong(value.intValue())

    override def boolean(value: Boolean) = Json.fromBoolean(value)

    override def nullNode() = Json.Null

    def encode[T](in: T)(implicit e: Encoder[T]) = e(in)

    def decode[T](in: Json)(implicit d: Decoder[T]) = d.decodeJson(in).getOrElse(throw new InvalidJsonForDecoding)

    /**
      * Convenience method for creating Body that just use straight JSON encoding/decoding logic
      */
    def body[R](description: Option[String] = None, example: R = null)
               (implicit encodec: Encoder[R], decodec: Decoder[R]) = Body(bodySpec[R](description)(encodec, decodec), example, ObjectParamType)

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
