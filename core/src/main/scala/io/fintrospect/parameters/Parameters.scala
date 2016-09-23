package io.fintrospect.parameters

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import java.util.UUID

import io.fintrospect.formats.{Argo, JsonFormat}
import io.fintrospect.parameters.StringValidation.EmptyIsValid

import scala.language.higherKinds
import scala.xml.Elem

/**
 * Prototype functions for creating parameters of various types.
 */
trait Parameters[P[_], R[_]] {

  /**
   * Create a parameter of a custom type. This will hook into pre-request validation (in terms of optional/mandatory parameters)
   * @param spec the parameter spec
   * @tparam T the type of the parameter
   * @return a parameter for retrieving a value of type [T] from the request
   */
  def apply[T](spec: ParameterSpec[T]): P[T] with R[T]

  /**
   * Create a parameter of a custom type using the spec supplied by the Supplier
   * @param parameterSpecSupplier provides a parameter spec
   * @tparam T the type of the parameter
   * @return a parameter for retrieving a value of type [T] from the request
   */
  def apply[T](parameterSpecSupplier: ParameterSpecSupplier[T]): P[T] with R[T] = apply(parameterSpecSupplier.spec)

  /**
   * Create a LocalDate parameter which is constrained by the format YYYY-MM-DD
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a LocalDate value from the request
   */
  def localDate(name: String, description: String = null): P[LocalDate] with R[LocalDate] = apply(ParameterSpec.localDate(name, description))

  /**
   * Create a ZonedDateTime parameter which is constrained by the format  YYYY-MM-DDTHH:mm:SSZ (See DateTimeFormatter.ISO_OFFSET_DATE_TIME)
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a ZonedDateTime value from the request
   */
  def zonedDateTime(name: String, description: String = null): P[ZonedDateTime] with R[ZonedDateTime] = apply(ParameterSpec.zonedDateTime(name, description))

  /**
   * Create a LocalDateTime parameter which is constrained by the format YYYY-MM-DDTHH:mm:SS
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a LocalDateTime value from the request
   */
  def dateTime(name: String, description: String = null): P[LocalDateTime] with R[LocalDateTime] = apply(ParameterSpec.dateTime(name, description))

  /**
   * Create a Boolean parameter which is constrained to boolean values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Boolean value from the request
   */
  def boolean(name: String, description: String = null): P[Boolean] with R[Boolean] = apply(ParameterSpec.boolean(name, description))

  /**
   * Create a String parameter which is not constrained
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @param validation validation mode for String values
   * @return a parameter for retrieving a String value from the request
   */
  def string(name: String, description: String = null, validation: StringValidation = EmptyIsValid): P[String] with R[String] = apply(ParameterSpec.string(name, description, validation))

  /**
    * Create a UUID parameter
    * @param name the name of the parameter (for use in description endpoints)
    * @param description optional description of the parameter (for use in description endpoints)
    * @return a parameter for retrieving a UUID value from the request
    */
  def uuid(name: String, description: String = null): P[UUID] with R[UUID] = apply(ParameterSpec.uuid(name, description))

  /**
   * Create a BigDecimal parameter which is constrained to numeric values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a BigDecimal value from the request
   */
  def bigDecimal(name: String, description: String = null): P[BigDecimal] with R[BigDecimal] = apply(ParameterSpec.bigDecimal(name, description))

  /**
   * Create a Long parameter which is constrained to numeric Long values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Long value from the request
   */
  def long(name: String, description: String = null): P[Long] with R[Long] = apply(ParameterSpec.long(name, description))

  /**
   * Create a Scala Int parameter which is constrained to numeric Int values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Int value from the request
   */
  def int(name: String, description: String = null): P[Int] with R[Int] = apply(ParameterSpec.int(name, description))

  /**
   * Create a Java Integer parameter which is constrained to numeric Integer values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Integer value from the request
   */
  def integer(name: String, description: String = null): P[Integer] with R[Integer] = apply(ParameterSpec.integer(name, description))

  /**
   * Create a Json-format JsonNode parameter which is constrained to values which parse to valid JSON objects
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a JsonNode value from the request
   */
  def json[T](name: String, description: String = null, format: JsonFormat[T, _] = Argo.JsonFormat): P[T] with R[T] = apply(ParameterSpec.json(name, description, format))

  /**
   * Create a native Scala XML-format parameter which is constrained to values which parse to valid XML objects
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a JsonNode value from the request
   */
  def xml(name: String, description: String = null): P[Elem] with R[Elem] = apply(ParameterSpec.xml(name, description))
}











