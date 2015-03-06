package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.{path => fp}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis

import scala.util.Try

object SegmentMatchers {

  def fixed(value: String): SegmentMatcher[String] = new SegmentMatcher[String] {
    override def unapply(str: String): Option[String] = if (str == value) Some(str) else None

    override def toString: String = value
  }

  def dateTime(name: String): SegmentMatcher[DateTime] = new NamedArgSegmentMatcher[DateTime](name) {
    override def unapply(str: String): Option[DateTime] = Some(dateTimeNoMillis().parseDateTime(str))
  }

  def boolean(name: String): SegmentMatcher[Boolean] = new NamedArgSegmentMatcher[Boolean](name) {
    override def unapply(str: String): Option[Boolean] = Try(str.toBoolean).toOption
  }

  def string(name: String): SegmentMatcher[String] = new NamedArgSegmentMatcher[String](name) {
    override def unapply(str: String): Option[String] = Some(str)
  }

  def long(name: String): SegmentMatcher[Long] = new NamedArgSegmentMatcher[Long](name) {
    override def unapply(str: String): Option[Long] = fp.Long.unapply(str)
  }

  def int(name: String): SegmentMatcher[Int] = new NamedArgSegmentMatcher[Int](name) {
    override def unapply(str: String): Option[Int] = fp.Integer.unapply(str)
  }

  def integer(name: String): SegmentMatcher[Integer] = new NamedArgSegmentMatcher[Integer](name) {
    override def unapply(str: String): Option[Integer] = fp.Integer.unapply(str).map(new Integer(_))
  }
}
