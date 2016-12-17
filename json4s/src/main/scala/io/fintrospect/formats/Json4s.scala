package io.fintrospect.formats

import java.math.BigInteger

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.ResponseSpec
import io.fintrospect.parameters.{Body, BodySpec, ParameterSpec}
import org.json4s.Extraction.decompose
import org.json4s.native.Document
import org.json4s.{Formats, JValue, JsonMethods, NoTypeHints, Serialization}

class Json4sFormat[T](jsonMethods: JsonMethods[T],
                      val serialization: Serialization,
                      useBigDecimalForDouble: Boolean) extends JsonFormat[JValue, JValue] {

  import org.json4s._

  override def pretty(in: JValue): String = jsonMethods.pretty(jsonMethods.render(in))

  override def parse(in: String): JValue = jsonMethods.parse(in, useBigDecimalForDouble)

  override def compact(in: JValue): String = jsonMethods.compact(jsonMethods.render(in))

  override def obj(fields: Iterable[Field]): JValue = JObject(fields.toList)

  override def string(value: String): JValue = JString(value)

  override def array(elements: Iterable[JValue]): JValue = JArray(elements.toList)

  override def boolean(value: Boolean): JValue = JBool(value)

  override def number(value: Int): JValue = JInt(value)

  override def number(value: BigDecimal): JValue = JDecimal(value)

  override def number(value: Long): JValue = JInt(value)

  override def number(value: BigInteger): JValue = JInt(value)

  override def nullNode(): JValue = JNull

  def encode[R](in: R, formats: Formats = serialization.formats(NoTypeHints)): JValue = decompose(in)(formats)

  def decode[R](in: JValue,
                formats: Formats = serialization.formats(NoTypeHints))
               (implicit mf: Manifest[R]): R = in.extract[R](formats, mf)
}

/**
  * Auto-marshalling filters which can be used to create Services which take and return domain objects
  * instead of HTTP responses
  */
class Json4sFilters[T](protected val jsonLibrary: Json4sLibrary[T])
  extends AutoFilters[JValue] {

  override protected val responseBuilder = jsonLibrary.ResponseBuilder

  private val formats = jsonLibrary.JsonFormat.serialization.formats(NoTypeHints)
  private val json4sFormat = jsonLibrary.JsonFormat

  import responseBuilder.implicits._


  private def toResponse[OUT](successStatus: Status, formats: Formats = formats) =
    (t: OUT) => {
      successStatus(json4sFormat.encode(t, formats))
    }

  private def toBody[BODY](mf: Manifest[BODY])(implicit example: BODY = null) =
    Body[BODY](jsonLibrary.bodySpec[BODY](None)(mf), example)


  /**
    * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
    * which return an object.
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def AutoInOut[BODY, OUT](svc: Service[BODY, OUT], successStatus: Status = Ok,
                                     formats: Formats = formats)
                                    (implicit example: BODY = null, mf: Manifest[BODY])
  : Service[Request, Response] = AutoInOutFilter(successStatus)(example, mf).andThen(svc)

  /**
    * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
    * which may return an object.
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def AutoInOptionalOut[BODY, OUT](svc: Service[BODY, Option[OUT]], successStatus: Status = Ok,
                                             formats: Formats = formats)
                                            (implicit example: BODY = null, mf: Manifest[BODY])
  : Service[Request, Response] = _AutoInOptionalOut(svc, toBody(mf), toResponse(successStatus, formats))

  /**
    * Filter to provide auto-marshalling of output case class instances for HTTP scenarios where an object is returned.
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def AutoOut[IN, OUT]
  (successStatus: Status = Ok, formats: Formats = formats): Filter[IN, Response, IN, OUT]
  = _AutoOut(toResponse(successStatus, formats))

  /**
    * Filter to provide auto-marshalling of case class instances for HTTP scenarios where an object may not be returned
    * HTTP OK is returned by default in the auto-marshalled response (overridable), otherwise a 404 is returned
    */
  def AutoOptionalOut[IN, OUT]
  (successStatus: Status = Ok, formats: Formats = formats)
  : Filter[IN, Response, IN, Option[OUT]]
  = _AutoOptionalOut((t: OUT) => successStatus(json4sFormat.encode(t, formats)))

  /**
    * Filter to provide auto-marshalling of case class instances for HTTP POST scenarios
    * HTTP OK is returned by default in the auto-marshalled response (overridable).
    */
  def AutoInOutFilter[BODY, OUT](successStatus: Status = Ok,
                                           formats: Formats = formats)
                                          (implicit example: BODY = null, mf: Manifest[BODY])
  : Filter[Request, Response, BODY, OUT] = AutoIn(toBody(mf)).andThen(AutoOut[BODY, OUT](successStatus, formats))
}

abstract class Json4sLibrary[T] extends JsonLibrary[JValue, JValue] {

  val JsonFormat: Json4sFormat[T]

  import JsonFormat._
  /**
    * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
    */
  def bodySpec[R](description: Option[String] = None, formats: Formats = serialization.formats(NoTypeHints))
                 (implicit mf: Manifest[R]) =
    BodySpec.json(description, JsonFormat).map(j => decode[R](j, formats)(mf), (u: R) => encode(u))

  /**
    * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
    */
  def responseSpec[R](statusAndDescription: (Status, String), example: R, formats: Formats = serialization.formats(NoTypeHints))
                     (implicit mf: Manifest[R]) =
    ResponseSpec.json(statusAndDescription, encode(example), JsonFormat)

  /**
    * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
    */
  def parameterSpec[R](name: String, description: Option[String] = None, formats: Formats = serialization.formats(NoTypeHints))
                      (implicit mf: Manifest[R]) =
    ParameterSpec.json(name, description.orNull, JsonFormat).map(j => decode[R](j, formats)(mf), (u: R) => encode(u))

}

/**
  * Native Json4S support (application/json content type) - uses BigDecimal for decimal
  */
object Json4s extends Json4sLibrary[Document] {

  val JsonFormat = new Json4sFormat(org.json4s.native.JsonMethods, org.json4s.native.Serialization, true)

  val Filters = new Json4sFilters(Json4s)
}

/**
  * Native Json4S support (application/json content type) - uses Doubles for decimal
  */
object Json4sDoubleMode extends Json4sLibrary[Document] {

  val JsonFormat = new Json4sFormat(org.json4s.native.JsonMethods, org.json4s.native.Serialization, false)

  val Filters = new Json4sFilters(Json4sDoubleMode)
}

/**
  * Jackson Json4S support (application/json content type) - uses BigDecimal for decimal
  */
object Json4sJackson extends Json4sLibrary[JValue] {

  val JsonFormat = new Json4sFormat(org.json4s.jackson.JsonMethods, org.json4s.jackson.Serialization, true)

  val Filters = new Json4sFilters(Json4sJackson)
}

/**
  * Jackson Json4S support (application/json content type) - uses Doubles for decimal
  */
object Json4sJacksonDoubleMode extends Json4sLibrary[JValue] {

  val JsonFormat = new Json4sFormat(org.json4s.jackson.JsonMethods, org.json4s.jackson.Serialization, false)

  val Filters = new Json4sFilters(Json4sJacksonDoubleMode)
}
