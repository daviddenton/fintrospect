package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.path
import org.joda.time.format.ISODateTimeFormat._
import org.joda.time.{DateTime, LocalDate}

import scala.reflect.ClassTag
import scala.util.Try

abstract class Parameters[P[_] <: Parameter[_]] protected[parameters]() {
  def localDate(name: String, description: String = null): P[LocalDate] = create(name, Option(description), str => Try(date().parseLocalDate(str)).toOption)

  def dateTime(name: String, description: String = null): P[DateTime] = create(name, Option(description), str => Try(dateTimeNoMillis().parseDateTime(str)).toOption)

  def boolean(name: String, description: String = null): P[Boolean] = create(name, Option(description), str => Try(str.toBoolean).toOption)

  def string(name: String, description: String = null): P[String] = create(name, Option(description), Option(_))

  def long(name: String, description: String = null): P[Long] = create(name, Option(description), str => path.Long.unapply(str))

  def int(name: String, description: String = null): P[Int] = create(name, Option(description), str => path.Integer.unapply(str))

  def integer(name: String, description: String = null): P[Integer] = create(name, Option(description), str => path.Integer.unapply(str).map(new Integer(_)))

  protected def create[T](name: String, description: Option[String], parse: (String => Option[T]))(implicit ct: ClassTag[T]): P[T]
}







