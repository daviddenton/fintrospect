package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.{path => fp}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat._

import scala.util.Try

object Parameters {
  def dateTime(location: Location, name: String): RequestParameter[DateTime] = new RequestParameter(name, location, true, s => Try(dateTimeNoMillis().parseDateTime(s)).toOption)

  def boolean(location: Location, name: String): RequestParameter[Boolean] = new RequestParameter(name, location, true, s => Try(s.toBoolean).toOption)

  def string(location: Location, name: String): RequestParameter[String] = new RequestParameter(name, location, true, Some(_))

  def long(location: Location, name: String): RequestParameter[Long] = new RequestParameter(name, location, true, s => fp.Long.unapply(s))

  def int(location: Location, name: String): RequestParameter[Int] = new RequestParameter(name, location, true, s => fp.Integer.unapply(s))

  def integer(location: Location, name: String): RequestParameter[Integer] = new RequestParameter(name, location, true, s => fp.Integer.unapply(s).map(new Integer(_)))
}