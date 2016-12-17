package io.fintrospect.parameters

import java.time.format.DateTimeFormatter.{ISO_LOCAL_DATE, ISO_LOCAL_DATE_TIME, ISO_ZONED_DATE_TIME}
import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import java.util.UUID

import io.fintrospect.formats.{Argo, JsonFormat}

import scala.xml.{Elem, XML}

/**
  * Spec required to marshal and unmarshal a parameter of a custom type
  *
  * @param deserialize function to take the input string from the request and attempt to construct a deserialized instance of T. Exceptions are
  *                    automatically caught and translated into the appropriate result, so just concentrate on the Happy-path case
  * @param paramType   The parameter type to be used in the documentation. For custom types, this is usually ObjectParamType (for JSON) or StringParamType
  * @param serialize   function to take the input type and serialize it to a string to be represented in the request
  * @param description optional description of the parameter (for use in description endpoints)
  * @tparam T the type of the parameter
  * @return a parameter for retrieving a value of type [T] from the request
  */
case class ParameterSpec[T](name: String,
                            description: Option[String] = None,
                            paramType: ParamType,
                            deserialize: String => T,
                            serialize: T => String = (s: T) => s.toString) {
  /**
    * Combined map and reverse-map operation
    */
  def map[O](in: T => O, out: O => T): ParameterSpec[O] = ParameterSpec[O](name, description, paramType, s => in(deserialize(s)), b => serialize(out(b)))

  /**
    * Traditional map operation. Duh.
    */
  def map[O](in: T => O) = ParameterSpec[O](name, description, paramType, s => in(deserialize(s)))
}

/**
  * Predefined ParameterSpec instances for common types
  */
object ParameterSpec {

  def localDate(name: String, description: String = null) = string(name, description).map(LocalDate.parse, (i: LocalDate) => ISO_LOCAL_DATE.format(i))

  def zonedDateTime(name: String, description: String = null) = string(name, description).map(ZonedDateTime.parse, (i: ZonedDateTime) => ISO_ZONED_DATE_TIME.format(i))

  def dateTime(name: String, description: String = null) = string(name, description).map(LocalDateTime.parse, (i: LocalDateTime) => ISO_LOCAL_DATE_TIME.format(i))

  def boolean(name: String, description: String = null) = ParameterSpec[Boolean](name, Option(description), BooleanParamType, _.toBoolean, _.toString)

  def string(name: String, description: String = null, validation: StringValidations.Rule = StringValidations.EmptyIsValid) = ParameterSpec[String](name, Option(description), StringParamType, validation, _.toString)

  def uuid(name: String, description: String = null) = string(name, description).map(UUID.fromString)

  def bigDecimal(name: String, description: String = null) = ParameterSpec[BigDecimal](name, Option(description), NumberParamType, BigDecimal(_), _.toString())

  def long(name: String, description: String = null) = ParameterSpec[Long](name, Option(description), IntegerParamType, _.toLong, _.toString)

  def int(name: String, description: String = null) = ParameterSpec[Int](name, Option(description), IntegerParamType, _.toInt, _.toString)

  def integer(name: String, description: String = null) = ParameterSpec[Integer](name, Option(description), IntegerParamType, new Integer(_), _.toString)

  def json[T](name: String, description: String = null, format: JsonFormat[T, _] = Argo.JsonFormat) = ParameterSpec[T](name, Option(description), ObjectParamType, format.parse, format.compact)

  def xml(name: String, description: String = null) = ParameterSpec[Elem](name, Option(description), StringParamType, XML.loadString, _.toString())
}
