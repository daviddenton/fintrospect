package io.fintrospect.formats.json

import java.math.BigInteger

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import io.circe._
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.ResponseSpec
import io.fintrospect.formats.json.JsonFormat.{InvalidJson, InvalidJsonForDecoding}
import io.fintrospect.parameters.{UniBody, Body, BodySpec, ObjectParamType, ParameterSpec}

/**
  * Circe JSON support (application/json content type)
  */
object Circe extends JsonLibrary[Json, Json] {

  object Filters {
    def Auto[BODY, OUT](svc: Service[BODY, OUT])(implicit db: Decoder[BODY], eb: Encoder[BODY], e: Encoder[OUT], example: BODY = null): Service[Request, Response] = {
      val body = Body[BODY](Circe.JsonFormat.bodySpec[BODY](None)(eb, db), example, ObjectParamType)
      marshallBody[BODY, Response](body).andThen(marshallOut[BODY, OUT](e)).andThen(svc)
    }

    def AutoFilter[BODY, OUT](implicit db: Decoder[BODY], eb: Encoder[BODY], e: Encoder[OUT], example: BODY = null): Filter[Request, Response, BODY, OUT] = {
      val body = Body[BODY](Circe.JsonFormat.bodySpec[BODY](None)(eb, db), example, ObjectParamType)
      marshallBody[BODY, Response](body).andThen(marshallOut[BODY, OUT](e))
    }

    def marshallBody[IN, OUT](body: Body[IN]): Filter[Request, OUT, IN, OUT] = new Filter[Request, OUT, IN, OUT] {
      override def apply(request: Request, service: Service[IN, OUT]): Future[OUT] = service(body <-- request)
    }

    def marshallOut[IN, OUT](implicit e: Encoder[OUT]): Filter[IN, Response, IN, OUT] = new Filter[IN, Response, IN, OUT] {
      override def apply(in: IN, service: Service[IN, OUT]): Future[Response] = {
        import Circe.ResponseBuilder.implicits._
        service(in).map(t => Ok(Circe.JsonFormat.encode(t)))
      }
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
