package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.path
import io.github.daviddenton.fintrospect.parameters.Requirement._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat._

import scala.reflect.ClassTag
import scala.util.Try

abstract class Parameters[P[_] <: Parameter[_]] protected[parameters]() {
  def dateTime(name: String, required: Requirement = Mandatory): P[DateTime] = create(name, required, str => Try(dateTimeNoMillis().parseDateTime(str)).toOption)

  def boolean(name: String, required: Requirement = Mandatory): P[Boolean] = create(name, required, str => Try(str.toBoolean).toOption)

  def string(name: String, required: Requirement = Mandatory): P[String] = create(name, required, Option(_))

  def long(name: String, required: Requirement = Mandatory): P[Long] = create(name, required, str => path.Long.unapply(str))

  def int(name: String, required: Requirement = Mandatory): P[Int] = create(name, required, str => path.Integer.unapply(str))

  def integer(name: String, required: Requirement = Mandatory): P[Integer] = create(name, required, str => path.Integer.unapply(str).map(new Integer(_)))

  protected def create[T](name: String, required: Requirement, parse: (String => Option[T]))(implicit ct: ClassTag[T]): P[T]
}







