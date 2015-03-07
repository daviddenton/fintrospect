package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path
import io.github.daviddenton.fintrospect.Locations._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat._

import scala.util.Try

class Parameters private(location: Location) {
  def dateTime(name: String): RequestParameter[DateTime] = new RequestParameter[DateTime](name, location, true, str => Try(dateTimeNoMillis().parseDateTime(str)).toOption)

  def boolean(name: String): RequestParameter[Boolean] = new RequestParameter[Boolean](name, location, true, str => Try(str.toBoolean).toOption)

  def string(name: String): RequestParameter[String] = new RequestParameter[String](name, location, true, Option(_))

  def long(name: String): RequestParameter[Long] = new RequestParameter[Long](name, location, true, str => path.Long.unapply(str))

  def int(name: String): RequestParameter[Int] = new RequestParameter[Int](name, location, true, str => path.Integer.unapply(str))

  def integer(name: String): RequestParameter[Integer] = new RequestParameter[Integer](name, location, true, str => path.Integer.unapply(str).map(new Integer(_)))
}

object Parameters {
  val Header = new Parameters(HeaderLocation)
  val Path = new Parameters(PathLocation)
  val Query = new Parameters(QueryLocation)
}