package io.fintrospect.formats.json

import java.math.BigInteger

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.ResponseSpec
import io.fintrospect.formats.json.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Body, BodySpec, ObjectParamType, ParameterSpec}
import play.api.libs.json.{Json, _}

/**
  * Play JSON support (application/json content type)
  */
object Play extends JsonLibrary[JsValue, JsValue] {

  /**
    * Auto-marshalling filters which can be used to create Services which take and return domain objects
    * instead of HTTP responses
    */
  object Filters extends AbstractFilters(Play) {

    import Play.ResponseBuilder.implicits._

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOut[BODY, OUT](svc: Service[BODY, OUT], successStatus: Status = Ok)
                            (implicit db: Reads[BODY], eb: Writes[BODY], e: Writes[OUT], example: BODY = null)
    : Service[Request, Response] = {
      val body = Body[BODY](Play.JsonFormat.bodySpec[BODY](None)(db, eb), example, ObjectParamType)
      AutoIn[BODY, Response](body).andThen(AutoOut[BODY, OUT](successStatus)(e)).andThen(svc)
    }

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which may return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoInOptionalOut[BODY, OUT](svc: Service[BODY, Option[OUT]], successStatus: Status = Ok)
                                    (implicit db: Reads[BODY], eb: Writes[BODY], e: Writes[OUT], example: BODY = null)
    : Service[Request, Response] = {
      val body = Body[BODY](Play.JsonFormat.bodySpec[BODY](None)(db, eb), example, ObjectParamType)
      AutoIn[BODY, Response](body).andThen(AutoOptionalOut[BODY, OUT](successStatus)(e)).andThen(svc)
    }

    /**
      * Filter to provide auto-marshalling of output case class instances for HTTP scenarios where an object is returned.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoOut[IN, OUT](successStatus: Status = Ok)
                        (implicit e: Writes[OUT]): Filter[IN, Response, IN, OUT]
    = _AutoOut((t:OUT)  => successStatus(Play.JsonFormat.encode(t)(e)))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP scenarios where an object may not be returned
      * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
      */
    def AutoOptionalOut[IN, OUT](successStatus: Status = Ok)
                                (implicit e: Writes[OUT]): Filter[IN, Response, IN, Option[OUT]]
    = _AutoOptionalOut((t: OUT) => successStatus(Play.JsonFormat.encode(t)(e)))

    /**
      * Filter to provide auto-marshalling of case class instances for HTTP POST scenarios
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOutFilter[BODY, OUT]
    (implicit successStatus: Status = Ok, db: Reads[BODY], eb: Writes[BODY], e: Writes[OUT], example: BODY = null)
    : Filter[Request, Response, BODY, OUT] = {
      val body = Body[BODY](Play.JsonFormat.bodySpec[BODY](None)(db, eb), example, ObjectParamType)
      AutoIn[BODY, Response](body).andThen(AutoOut[BODY, OUT](successStatus)(e))
    }
  }

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
