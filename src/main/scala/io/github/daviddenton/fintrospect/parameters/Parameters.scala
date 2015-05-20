package io.github.daviddenton.fintrospect.parameters

import argo.jdom.JsonNode
import io.github.daviddenton.fintrospect.util.ArgoUtil
import org.joda.time.format.ISODateTimeFormat._
import org.joda.time.{DateTime, LocalDate}

import scala.language.higherKinds
import scala.util.Try

/**
 * Prototype functions for creating parameters of various types.
 */
class Parameters[P[_]] protected[parameters](builder: ParameterBuilder[P]) {
  def localDate(name: String, description: String = null): P[LocalDate] = builder.apply(name, Option(description), StringParamType, str => Try(date().parseLocalDate(str)))

  def dateTime(name: String, description: String = null): P[DateTime] = builder.apply(name, Option(description), StringParamType, str => Try(dateTimeNoMillis().parseDateTime(str)))

  def boolean(name: String, description: String = null): P[Boolean] = builder.apply(name, Option(description), BooleanParamType, str => Try(str.toBoolean))

  def string(name: String, description: String = null): P[String] = builder.apply(name, Option(description), StringParamType, Try(_))

  def bigDecimal(name: String, description: String = null): P[BigDecimal] = builder.apply(name, Option(description), NumberParamType, str => Try(BigDecimal(str)))

  def long(name: String, description: String = null): P[Long] = builder.apply(name, Option(description), IntegerParamType, str => Try(str.toLong))

  def int(name: String, description: String = null): P[Int] = builder.apply(name, Option(description), IntegerParamType, str => Try(str.toInt))

  def integer(name: String, description: String = null): P[Integer] = builder.apply(name, Option(description), IntegerParamType, str => Try(str.toInt))

  def json(name: String, description: String = null): P[JsonNode] = builder.apply(name, Option(description), ObjectParamType, str => Try(ArgoUtil.parse(str)))
}









