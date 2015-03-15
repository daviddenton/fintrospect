package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.path
import io.github.daviddenton.fintrospect.parameters.Requirement._
import org.joda.time.{LocalDate, DateTime}
import org.joda.time.format.ISODateTimeFormat._

import scala.reflect.ClassTag
import scala.util.Try

abstract class Parameters[P[_] <: Parameter[_]] protected[parameters]() {
  def localDate(name: String, description: String = null, required: Requirement = Mandatory): P[LocalDate] = create(name, Option(description), required, str => Try(date().parseLocalDate(str)).toOption)

  def dateTime(name: String, description: String = null, required: Requirement = Mandatory): P[DateTime] = create(name, Option(description), required, str => Try(dateTimeNoMillis().parseDateTime(str)).toOption)

  def boolean(name: String, description: String = null, required: Requirement = Mandatory): P[Boolean] = create(name, Option(description), required, str => Try(str.toBoolean).toOption)

  def string(name: String, description: String = null, required: Requirement = Mandatory): P[String] = create(name, Option(description), required, Option(_))

  def long(name: String, description: String = null, required: Requirement = Mandatory): P[Long] = create(name, Option(description), required, str => path.Long.unapply(str))

  def int(name: String, description: String = null, required: Requirement = Mandatory): P[Int] = create(name, Option(description), required, str => path.Integer.unapply(str))

  def integer(name: String, description: String = null, required: Requirement = Mandatory): P[Integer] = create(name, Option(description), required, str => path.Integer.unapply(str).map(new Integer(_)))

  protected def create[T](name: String, description: Option[String], required: Requirement, parse: (String => Option[T]))(implicit ct: ClassTag[T]): P[T]
}







