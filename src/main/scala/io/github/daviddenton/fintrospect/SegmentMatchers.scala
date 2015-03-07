package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.Parameters._
import org.joda.time.DateTime

object SegmentMatchers {

  def fixed(value: String): SegmentMatcher[String] = new SegmentMatcher[String] {
    val toParameter: Option[Parameter[_]] = None
    override def unapply(str: String): Option[String] = if (str == value) Some(str) else None
    override def toString: String = value
  }

  def dateTime(name: String): SegmentMatcher[DateTime] = Path.dateTime(name)

  def boolean(name: String): SegmentMatcher[Boolean] = Path.boolean(name)

  def string(name: String): SegmentMatcher[String] = Path.string( name)

  def long(name: String): SegmentMatcher[Long] = Path.long(name)

  def int(name: String): SegmentMatcher[Int] = Path.int(name)

  def integer(name: String): SegmentMatcher[Integer] = Path.integer(name)
}
