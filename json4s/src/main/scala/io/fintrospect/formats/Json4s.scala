package io.fintrospect.formats

import java.math.BigInteger

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.ResponseSpec
import io.fintrospect.parameters.{Body, BodySpec, ObjectParamType, ParameterSpec}
import org.json4s.Extraction.decompose
import org.json4s.{Formats, JValue, JsonMethods, NoTypeHints, Serialization}

class Json4sFormat[T](jsonMethods: JsonMethods[T],
                      val serialization: Serialization,
                      useBigDecimalForDouble: Boolean) extends JsonFormat[JValue, JValue] {

  import org.json4s._

  override def pretty(in: JValue): String = jsonMethods.pretty(jsonMethods.render(in))

  override def parse(in: String): JValue = jsonMethods.parse(in, useBigDecimalForDouble)

  override def compact(in: JValue): String = jsonMethods.compact(jsonMethods.render(in))

  override def obj(fields: Iterable[Field]): JValue = JObject(fields.toList)

  override def obj(fields: (String, JValue)*): JValue = JObject(fields: _*)

  override def string(value: String): JValue = JString(value)

  override def array(elements: JValue*): JValue = JArray(elements.toList)

  override def array(elements: Iterable[JValue]): JValue = JArray(elements.toList)

  override def boolean(value: Boolean): JValue = JBool(value)

  override def number(value: Int): JValue = JInt(value)

  override def number(value: BigDecimal): JValue = JDecimal(value)

  override def number(value: Long): JValue = JInt(value)

  override def number(value: BigInteger): JValue = JInt(value)

  override def nullNode(): JValue = JNull

  def encode(in: AnyRef, formats: Formats = serialization.formats(NoTypeHints)): JValue = decompose(in)(formats)

  def decode[R](in: JValue,
                formats: Formats = serialization.formats(NoTypeHints))
               (implicit mf: scala.reflect.Manifest[R]): R = in.extract[R](formats, mf)

  /**
    * Convenience method for creating Body that just use straight JSON encoding/decoding logic
    */
  def body[R](description: Option[String] = None, example: R = null, formats: Formats = serialization.formats(NoTypeHints))
             (implicit mf: scala.reflect.Manifest[R]) = Body(bodySpec[R](description, formats)(mf), example, ObjectParamType)

  /**
    * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
    */
  def bodySpec[R](description: Option[String] = None, formats: Formats = serialization.formats(NoTypeHints))
                 (implicit mf: scala.reflect.Manifest[R]) =
  BodySpec.string(description, APPLICATION_JSON).map(s => decode[R](parse(s), formats)(mf), (u: R) => compact(encode(u.asInstanceOf[AnyRef])))

  /**
    * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
    */
  def responseSpec[R](statusAndDescription: (Status, String), example: R, formats: Formats = serialization.formats(NoTypeHints))
                     (implicit mf: scala.reflect.Manifest[R]) =
  ResponseSpec.json(statusAndDescription, encode(example.asInstanceOf[AnyRef]), this)

  /**
    * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
    */
  def parameterSpec[R](name: String, description: Option[String] = None, formats: Formats = serialization.formats(NoTypeHints))
                      (implicit mf: scala.reflect.Manifest[R]) =
  ParameterSpec[R](name, description, ObjectParamType,
    s => decode[R](parse(s), formats)(mf),
    (u: R) => compact(encode(u.asInstanceOf[AnyRef])))
}

/**
  * Auto-marshalling filters which can be used to create Services which take and return domain objects
  * instead of HTTP responses
  */
class Json4sFilters[T](json4sFormat: Json4sFormat[T], protected val jsonLibrary: JsonLibrary[JValue, JValue])
  extends AutoFilters[JValue] {

  override protected val responseBuilder = jsonLibrary.ResponseBuilder
  import responseBuilder.implicits._

  private val a = json4sFormat.serialization.formats(NoTypeHints)

  private def toResponse[OUT <: AnyRef](successStatus: Status, formats: Formats = a) =
    (t: OUT) => {
      successStatus(json4sFormat.encode(t, formats))
    }

  private def toBody[BODY](mf: scala.reflect.Manifest[BODY])(implicit example: BODY = null) =
    Body[BODY](json4sFormat.bodySpec[BODY](None)(mf), example, ObjectParamType)


  /**
    * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
    * which return an object.
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def AutoInOut[BODY, OUT <: AnyRef](svc: Service[BODY, OUT], successStatus: Status = Ok,
                                     formats: Formats = a)
                                    (implicit example: BODY = null, mf: scala.reflect.Manifest[BODY])
  : Service[Request, Response] = AutoInOutFilter(successStatus)(example, mf).andThen(svc)

  /**
    * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
    * which may return an object.
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def AutoInOptionalOut[BODY, OUT <: AnyRef](svc: Service[BODY, Option[OUT]], successStatus: Status = Ok,
                                             formats: Formats = a)
                                            (implicit example: BODY = null, mf: scala.reflect.Manifest[BODY])
  : Service[Request, Response] = _AutoInOptionalOut(svc, toBody(mf), toResponse(successStatus, formats))

  /**
    * Filter to provide auto-marshalling of output case class instances for HTTP scenarios where an object is returned.
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def AutoOut[IN, OUT <: AnyRef]
  (successStatus: Status = Ok, formats: Formats = a): Filter[IN, Response, IN, OUT]
  = _AutoOut(toResponse(successStatus, formats))

  /**
    * Filter to provide auto-marshalling of case class instances for HTTP scenarios where an object may not be returned
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def AutoOptionalOut[IN, OUT <: AnyRef]
  (successStatus: Status = Ok, formats: Formats = a)
  : Filter[IN, Response, IN, Option[OUT]]
  = _AutoOptionalOut((t: OUT) => successStatus(json4sFormat.encode(t, formats)))

  /**
    * Filter to provide auto-marshalling of case class instances for HTTP POST scenarios
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def AutoInOutFilter[BODY, OUT <: AnyRef](successStatus: Status = Ok,
                                           formats: Formats = a)
                                          (implicit example: BODY = null, mf: scala.reflect.Manifest[BODY])
  : Filter[Request, Response, BODY, OUT] = AutoIn(toBody(mf)).andThen(AutoOut[BODY, OUT](successStatus, formats))
}

/**
  * Native Json4S support (application/json content type) - uses BigDecimal for decimal
  */
object Json4s extends JsonLibrary[JValue, JValue] {

  val JsonFormat = new Json4sFormat(org.json4s.native.JsonMethods, org.json4s.native.Serialization, true)

  val Filters = new Json4sFilters(JsonFormat, Json4s)
}

/**
  * Native Json4S support (application/json content type) - uses Doubles for decimal
  */
object Json4sDoubleMode extends JsonLibrary[JValue, JValue] {

  val JsonFormat = new Json4sFormat(org.json4s.native.JsonMethods, org.json4s.native.Serialization, false)

  val Filters = new Json4sFilters(JsonFormat, Json4sDoubleMode)
}

/**
  * Jackson Json4S support (application/json content type) - uses BigDecimal for decimal
  */
object Json4sJackson extends JsonLibrary[JValue, JValue] {

  val JsonFormat = new Json4sFormat(org.json4s.jackson.JsonMethods, org.json4s.jackson.Serialization, true)

  val Filters = new Json4sFilters(JsonFormat, Json4sJackson)
}

/**
  * Jackson Json4S support (application/json content type) - uses Doubles for decimal
  */
object Json4sJacksonDoubleMode extends JsonLibrary[JValue, JValue] {

  val JsonFormat = new Json4sFormat(org.json4s.jackson.JsonMethods, org.json4s.jackson.Serialization, false)

  val Filters = new Json4sFilters(JsonFormat, Json4sJacksonDoubleMode)
}
