package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path
import io.github.daviddenton.fintrospect.Locations._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat._

import scala.reflect.ClassTag
import scala.util.Try

abstract class Parameters[P[_] <: Parameter[_]] {
  def dateTime(name: String): P[DateTime] = create(name, required = true, str => Try(dateTimeNoMillis().parseDateTime(str)).toOption)

  def boolean(name: String): P[Boolean] = create(name, required = true, str => Try(str.toBoolean).toOption)

  def string(name: String): P[String] = create(name, required = true, Option(_))

  def long(name: String): P[Long] = create(name, required = true, str => path.Long.unapply(str))

  def int(name: String): P[Int] = create(name, required = true, str => path.Integer.unapply(str))

  def integer(name: String): P[Integer] = create(name, required = true, str => path.Integer.unapply(str).map(new Integer(_)))

  def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]): P[T]
}

object Parameters {
  val Header = new Parameters[RequestParameter] {
    def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new RequestParameter[T](name, HeaderLocation, required, parse)
  }

  val Path = new Parameters[PathParameter] {
    def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new PathParameter[T](name, parse)
  }

  val Query = new Parameters[RequestParameter] {
    def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new RequestParameter[T](name, QueryLocation, required, parse)
  }
}