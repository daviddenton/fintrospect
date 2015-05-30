package io.github.daviddenton.fintrospect.parameters

import java.time.{LocalDateTime, LocalDate, ZonedDateTime}

import argo.jdom.JsonNode
import io.github.daviddenton.fintrospect.util.ArgoUtil

import scala.language.higherKinds
import scala.util.Try

/**
 * Prototype functions for creating parameters of various types.
 */
class Parameters[P[_]] protected[parameters](builder: ParameterBuilder[P]) {
  def localDate(name: String, description: String = null): P[LocalDate] = builder.apply(name, Option(description), StringParamType, str => Try(LocalDate.parse(str)))

  def zonedDateTime(name: String, description: String = null): P[ZonedDateTime] = builder.apply(name, Option(description), StringParamType, str => Try(ZonedDateTime.parse(str)))

  def dateTime(name: String, description: String = null): P[LocalDateTime] = builder.apply(name, Option(description), StringParamType, str => Try(LocalDateTime.parse(str)))

  def boolean(name: String, description: String = null): P[Boolean] = builder.apply(name, Option(description), BooleanParamType, str => Try(str.toBoolean))

  def string(name: String, description: String = null): P[String] = builder.apply(name, Option(description), StringParamType, Try(_))

  def bigDecimal(name: String, description: String = null): P[BigDecimal] = builder.apply(name, Option(description), NumberParamType, str => Try(BigDecimal(str)))

  def long(name: String, description: String = null): P[Long] = builder.apply(name, Option(description), IntegerParamType, str => Try(str.toLong))

  def int(name: String, description: String = null): P[Int] = builder.apply(name, Option(description), IntegerParamType, str => Try(str.toInt))

  def integer(name: String, description: String = null): P[Integer] = builder.apply(name, Option(description), IntegerParamType, str => Try(str.toInt))

  def json(name: String, description: String = null): P[JsonNode] = builder.apply(name, Option(description), ObjectParamType, str => Try(ArgoUtil.parse(str)))
}









