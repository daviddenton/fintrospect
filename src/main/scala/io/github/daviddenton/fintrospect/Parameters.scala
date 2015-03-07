package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.{path => fp}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat._

import scala.util.Try

object Parameters {
  def dateTime(location: Location, name: String): RequestParameter[DateTime] = new RequestParameter[DateTime](name, location, true) {
    override def unapply(str: String): Option[DateTime] = Try(dateTimeNoMillis().parseDateTime(str)).toOption
  }

  def boolean(location: Location, name: String): RequestParameter[Boolean] = new RequestParameter[Boolean](name, location, true) {
    override def unapply(str: String): Option[Boolean] = Try(str.toBoolean).toOption
  }

  def string(location: Location, name: String): RequestParameter[String] = new RequestParameter[String](name, location, true) {
    override def unapply(str: String): Option[String] = Option(str)
  }

  def long(location: Location, name: String): RequestParameter[Long] = new RequestParameter[Long](name, location, true) {
    override def unapply(str: String): Option[Long] = fp.Long.unapply(str)
  }

  def int(location: Location, name: String): RequestParameter[Int] = new RequestParameter[Int](name, location, true) {
    override def unapply(str: String): Option[Int] = fp.Integer.unapply(str)
  }

  def integer(location: Location, name: String): RequestParameter[Integer] = new RequestParameter[Integer](name, location, true) {
    override def unapply(str: String): Option[Integer] = fp.Integer.unapply(str).map(new Integer(_))
  }
}