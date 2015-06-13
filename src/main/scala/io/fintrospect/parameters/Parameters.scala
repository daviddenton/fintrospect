package io.fintrospect.parameters

import java.time.format.DateTimeFormatter._
import java.time.{LocalDate, LocalDateTime, ZonedDateTime}

import argo.jdom.JsonRootNode
import io.fintrospect.util.ArgoUtil._

import scala.language.higherKinds




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
   * Create a parameter of a custom type. This will hook into pre-request validation (in terms of optional/mandatory parameters)
   * @param name the name of the parameter (for use in description endpoints)
   * @param deserialize function to take the input string from the request and attempt to construct a deserialized instance. Exceptions are
   *                    automatically caught and translated into the appropriate result, so just concentrate on the Happy-path case
   * @param serialize function to take the input type from the request and serialize it to a string to be represented
   * @param description optional description of the parameter (for use in description endpoints)
   * @tparam T the type of the parameter
   * @return a parameter for retrieving a value of type [T] from the request
   */
  def apply[T](name: String, deserialize: String => T, serialize: T => String, description: String = null): P[T] with R[T] =
    apply(ParameterSpec[T](name, Option(description), StringParamType, deserialize, serialize))

  /**
   * Create a LocalDate parameter which is constrained by the format YYYY-MM-DD
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a LocalDate value from the request
   */
  def localDate(name: String, description: String = null): P[LocalDate] with R[LocalDate] =
    apply(ParameterSpec(name, Option(description), StringParamType, LocalDate.parse(_), ISO_LOCAL_DATE.format(_)))

  /**
   * Create a ZonedDateTime parameter which is constrained by the format  YYYY-MM-DDTHH:mm:SSZ (See DateTimeFormatter.ISO_OFFSET_DATE_TIME)
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a ZonedDateTime value from the request
   */
  def zonedDateTime(name: String, description: String = null): P[ZonedDateTime] with R[ZonedDateTime] =
    apply(ParameterSpec(name, Option(description), StringParamType, ZonedDateTime.parse(_), ISO_ZONED_DATE_TIME.format(_)))

  /**
   * Create a LocalDateTime parameter which is constrained by the format YYYY-MM-DDTHH:mm:SS
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a LocalDateTime value from the request
   */
  def dateTime(name: String, description: String = null): P[LocalDateTime] with R[LocalDateTime] =
    apply(ParameterSpec(name, Option(description), StringParamType, LocalDateTime.parse(_), ISO_LOCAL_DATE_TIME.format(_)))

  /**
   * Create a Boolean parameter which is constrained to boolean values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Boolean value from the request
   */
  def boolean(name: String, description: String = null): P[Boolean] with R[Boolean] =
    apply(ParameterSpec(name, Option(description), BooleanParamType, _.toBoolean, _.toString))

  /**
   * Create a String parameter which is not constrained
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a String value from the request
   */
  def string(name: String, description: String = null): P[String] with R[String] =
    apply(ParameterSpec(name, Option(description), StringParamType, identity, _.toString))

  /**
   * Create a BigDecimal parameter which is constrained to numeric values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a BigDecimal value from the request
   */
  def bigDecimal(name: String, description: String = null): P[BigDecimal] with R[BigDecimal] =
    apply(ParameterSpec(name, Option(description), NumberParamType, BigDecimal(_), _.toString))

  /**
   * Create a Long parameter which is constrained to numeric Long values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Long value from the request
   */
  def long(name: String, description: String = null): P[Long] with R[Long] =
    apply(ParameterSpec(name, Option(description), IntegerParamType, _.toLong, _.toString))

  /**
   * Create a Scala Int parameter which is constrained to numeric Int values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Int value from the request
   */
  def int(name: String, description: String = null): P[Int] with R[Int] =
    apply(ParameterSpec(name, Option(description), IntegerParamType, _.toInt, _.toString))

  /**
   * Create a Java Integer parameter which is constrained to numeric Integer values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Integer value from the request
   */
  def integer(name: String, description: String = null): P[Integer] with R[Integer] =
    apply(ParameterSpec(name, Option(description), IntegerParamType, new Integer(_), _.toString))

  /**
   * Create a Argo-format JsonNode parameter which is constrained to values which parse to valid JSON objects
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a JsonNode value from the request
   */
  def json(name: String, description: String = null): P[JsonRootNode] with R[JsonRootNode] =
    apply(ParameterSpec(name, Option(description), ObjectParamType, parse, compact(_)))
}









