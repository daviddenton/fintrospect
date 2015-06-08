package io.fintrospect.parameters

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}

import argo.jdom.JsonRootNode
import io.fintrospect.util.ArgoUtil

import scala.language.higherKinds

/**
 * Prototype functions for creating parameters of various types.
 */
trait Parameters[P[_]] {

  protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => T)): P[T]

  /**
   * Create a parameter of a custom type. This will hook into pre-request validation (in terms of optional/mandatory parameters)
   * @param name the name of the parameter (for use in description endpoints)
   * @param parse function to take the input string from the request and attempt to construct a deserialized instance. Exceptions are
   *              automatically caught and translated into the appropriate result, so just concentrate on the Happy-path case
   * @param description optional description of the parameter (for use in description endpoints)
   * @tparam T the type of the parameter
   * @return a parameter for retrieving a value of type [T] from the request
   */
  def custom[T](name: String, parse: String => T, description: String = null): P[T] = parameter(name, Option(description), StringParamType, parse)

  /**
   * Create a LocalDate parameter which is constrained by the format YYYY-MM-DD
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a LocalDate value from the request
   */
  def localDate(name: String, description: String = null): P[LocalDate] = parameter(name, Option(description), StringParamType, LocalDate.parse(_))

  /**
   * Create a ZonedDateTime parameter which is constrained by the format  YYYY-MM-DDTHH:mm:SSZ (See DateTimeFormatter.ISO_OFFSET_DATE_TIME)
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a ZonedDateTime value from the request
   */
  def zonedDateTime(name: String, description: String = null): P[ZonedDateTime] = parameter(name, Option(description), StringParamType, ZonedDateTime.parse(_))

  /**
   * Create a LocalDateTime parameter which is constrained by the format YYYY-MM-DDTHH:mm:SS
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a LocalDateTime value from the request
   */
  def dateTime(name: String, description: String = null): P[LocalDateTime] = parameter(name, Option(description), StringParamType, LocalDateTime.parse(_))

  /**
   * Create a Boolean parameter which is constrained to boolean values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Boolean value from the request
   */
  def boolean(name: String, description: String = null): P[Boolean] = parameter(name, Option(description), BooleanParamType, _.toBoolean)

  /**
   * Create a String parameter which is not constrained
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a String value from the request
   */
  def string(name: String, description: String = null): P[String] = parameter(name, Option(description), StringParamType, identity)

  /**
   * Create a BigDecimal parameter which is constrained to numeric values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a BigDecimal value from the request
   */
  def bigDecimal(name: String, description: String = null): P[BigDecimal] = parameter(name, Option(description), NumberParamType, BigDecimal(_))

  /**
   * Create a Long parameter which is constrained to numeric Long values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Long value from the request
   */
  def long(name: String, description: String = null): P[Long] = parameter(name, Option(description), IntegerParamType, _.toLong)

  /**
   * Create a Scala Int parameter which is constrained to numeric Int values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Int value from the request
   */
  def int(name: String, description: String = null): P[Int] = parameter(name, Option(description), IntegerParamType, _.toInt)

  /**
   * Create a Java Integer parameter which is constrained to numeric Integer values
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a Integer value from the request
   */
  def integer(name: String, description: String = null): P[Integer] = parameter(name, Option(description), IntegerParamType, new Integer(_))

  /**
   * Create a Argo-format JsonNode parameter which is constrained to values which parse to valid JSON objects
   * @param name the name of the parameter (for use in description endpoints)
   * @param description optional description of the parameter (for use in description endpoints)
   * @return a parameter for retrieving a JsonNode value from the request
   */
  def json(name: String, description: String = null): P[JsonRootNode] = parameter(name, Option(description), ObjectParamType, ArgoUtil.parse)
}









