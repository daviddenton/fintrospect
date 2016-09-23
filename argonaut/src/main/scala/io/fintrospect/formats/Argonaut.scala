package io.fintrospect.formats

import java.math.BigInteger

import argonaut.Argonaut._
import argonaut.{DecodeJson, EncodeJson, Json}
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.ResponseSpec
import io.fintrospect.formats.JsonFormat.{InvalidJson, InvalidJsonForDecoding}
import io.fintrospect.parameters.{Body, BodySpec, ObjectParamType, ParameterSpec}

/**
  * Argonaut JSON support (application/json content type)
  */
object Argonaut extends JsonLibrary[Json, Json] {

  import Argonaut.ResponseBuilder.implicits._

  /**
    * Auto-marshalling filters which can be used to create Services which take and return domain objects
    * instead of HTTP responses
    */
  object Filters extends AutoFilters[Json] {

    override protected val responseBuilder = Argonaut.ResponseBuilder

    private def toResponse[OUT](successStatus: Status, e: EncodeJson[OUT]) =
      (t: OUT) => successStatus(Argonaut.JsonFormat.encode(t)(e))

    private def toBody[BODY](db: DecodeJson[BODY], eb: EncodeJson[BODY])(implicit example: BODY = null) =
      Body[BODY](Argonaut.JsonFormat.bodySpec[BODY](None)(eb, db), example, ObjectParamType)

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOut[BODY, OUT](svc: Service[BODY, OUT], successStatus: Status = Ok)
                            (implicit db: DecodeJson[BODY], eb: EncodeJson[BODY], e: EncodeJson[OUT], example: BODY = null)
    : Service[Request, Response] = AutoInOutFilter(successStatus)(db, eb, e, example).andThen(svc)

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which may return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoInOptionalOut[BODY, OUT](svc: Service[BODY, Option[OUT]], successStatus: Status = Ok)
                                    (implicit db: DecodeJson[BODY], eb: EncodeJson[BODY], e: EncodeJson[OUT], example: BODY = null)
    : Service[Request, Response] = _AutoInOptionalOut(svc, toBody(db, eb), toResponse(successStatus, e))

    /**
      * Filter to provide auto-marshalling of output case class instances for HTTP scenarios where an object is returned.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoOut[IN, OUT](successStatus: Status = Ok)
                        (implicit e: EncodeJson[OUT]): Filter[IN, Response, IN, OUT]
    = _AutoOut(toResponse(successStatus, e))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP scenarios where an object may not be returned
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoOptionalOut[IN, OUT](successStatus: Status = Ok)
                                (implicit e: EncodeJson[OUT]): Filter[IN, Response, IN, Option[OUT]]
    = _AutoOptionalOut(toResponse(successStatus, e))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP POST scenarios
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOutFilter[BODY, OUT](successStatus: Status = Ok)
                                  (implicit db: DecodeJson[BODY], eb: EncodeJson[BODY], e: EncodeJson[OUT], example: BODY = null)
    : Filter[Request, Response, BODY, OUT] = AutoIn(toBody(db, eb)).andThen(AutoOut[BODY, OUT](successStatus)(e))
  }

  object JsonFormat extends JsonFormat[Json, Json] {

    override def parse(in: String): Json = in.parseOption.getOrElse(throw new InvalidJson)

    override def pretty(node: Json): String = node.spaces2

    override def compact(node: Json): String = node.nospaces

    override def obj(fields: Iterable[Field]): Json = Json.obj(fields.map(f => (f._1, f._2)).toSeq: _*)

    override def obj(fields: Field*): Json = Json.obj(fields.map(f => (f._1, f._2)): _*)

    override def array(elements: Iterable[Json]) = Json.array(elements.toSeq: _*)

    override def array(elements: Json*) = array(elements)

    override def string(value: String) = jString(value)

    override def number(value: Int) = jNumber(value)

    override def number(value: BigDecimal) = jNumber(value.doubleValue())

    override def number(value: Long) = jNumber(value)

    override def number(value: BigInteger) = jNumber(value.intValue())

    override def boolean(value: Boolean) = jBool(value)

    override def nullNode() = jNull

    def encode[T](in: T)(implicit encodec: EncodeJson[T]) = encodec.encode(in)

    def decode[T](in: Json)(implicit decodec: DecodeJson[T]) = decodec.decodeJson(in).getOr(throw new InvalidJsonForDecoding)

    /**
      * Convenience method for creating Body that just use straight JSON encoding/decoding logic
      */
    def body[R](description: Option[String] = None, example: R = null)
               (implicit encodec: EncodeJson[R], decodec: DecodeJson[R]) = Body(bodySpec[R](description)(encodec, decodec), example, ObjectParamType)

    /**
      * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
      */
    def bodySpec[R](description: Option[String] = None)(implicit encodec: EncodeJson[R], decodec: DecodeJson[R]) =
      BodySpec.string(description, APPLICATION_JSON).map(s => decode[R](parse(s))(decodec), (u: R) => compact(encode(u)(encodec)))

    /**
      * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
      */
    def responseSpec[R](statusAndDescription: (Status, String), example: R)
                       (implicit encodec: EncodeJson[R], decodec: DecodeJson[R]) =
      ResponseSpec.json(statusAndDescription, encode(example)(encodec), this)

    /**
      * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
      */
    def parameterSpec[R](name: String, description: Option[String] = None)(implicit encodec: EncodeJson[R], decodec: DecodeJson[R]) =
      ParameterSpec[R](name, description, ObjectParamType, s => decode[R](parse(s))(decodec), (u: R) => compact(encode(u)(encodec)))
  }

}
