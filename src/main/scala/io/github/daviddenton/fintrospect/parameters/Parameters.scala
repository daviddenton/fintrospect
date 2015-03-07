package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.path
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat._

import scala.reflect.ClassTag
import scala.util.Try

abstract class Parameters[P[_] <: Parameter[_]] protected[parameters]() {
  def dateTime(name: String): P[DateTime] = create(name, required = true, str => Try(dateTimeNoMillis().parseDateTime(str)).toOption)

  def boolean(name: String): P[Boolean] = create(name, required = true, str => Try(str.toBoolean).toOption)

  def string(name: String): P[String] = create(name, required = true, Option(_))

  def long(name: String): P[Long] = create(name, required = true, str => path.Long.unapply(str))

  def int(name: String): P[Int] = create(name, required = true, str => path.Integer.unapply(str))

  def integer(name: String): P[Integer] = create(name, required = true, str => path.Integer.unapply(str).map(new Integer(_)))

  protected def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]): P[T]
}







