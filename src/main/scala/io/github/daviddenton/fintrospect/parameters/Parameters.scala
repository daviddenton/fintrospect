package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.path
import org.joda.time.format.ISODateTimeFormat._
import org.joda.time.{DateTime, LocalDate}

import scala.reflect.ClassTag
import scala.util.Try

class Parameters[P[_]] protected[parameters](builder: ParameterBuilder[P]) {
  def localDate(name: String, description: String = null): P[LocalDate] = builder.apply(name, Option(description), str => Try(date().parseLocalDate(str)).toOption)

  def dateTime(name: String, description: String = null): P[DateTime] = builder.apply(name, Option(description), str => Try(dateTimeNoMillis().parseDateTime(str)).toOption)

  def boolean(name: String, description: String = null): P[Boolean] = builder.apply(name, Option(description), str => Try(str.toBoolean).toOption)

  def string(name: String, description: String = null): P[String] = builder.apply(name, Option(description), Option(_))

  def long(name: String, description: String = null): P[Long] = builder.apply(name, Option(description), str => path.Long.unapply(str))

  def int(name: String, description: String = null): P[Int] = builder.apply(name, Option(description), str => path.Integer.unapply(str))

  def integer(name: String, description: String = null): P[Integer] = builder.apply(name, Option(description), str => path.Integer.unapply(str).map(new Integer(_)))
}

trait ParameterBuilder[P[_]] {
  def apply[T](name: String, description: Option[String], parse: (String => Option[T]))(implicit ct: ClassTag[T]): P[T]
}







