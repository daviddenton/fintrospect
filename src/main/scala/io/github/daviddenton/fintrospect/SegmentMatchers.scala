package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.Parameters._
import org.joda.time.DateTime

object SegmentMatchers {

  def fixed(value: String): SegmentMatcher[String] = new SegmentMatcher[String] {
    override def unapply(str: String): Option[String] = if (str == value) Some(str) else None
    override def toString: String = value
  }

  def dateTime(name: String): SegmentMatcher[DateTime] = new NamedArgSegmentMatcher(Path.dateTime(name))

  def boolean(name: String): SegmentMatcher[Boolean] = new NamedArgSegmentMatcher(Path.boolean(name))

  def string(name: String): SegmentMatcher[String] = new NamedArgSegmentMatcher(Path.string( name))

  def long(name: String): SegmentMatcher[Long] = new NamedArgSegmentMatcher(Path.long(name))

  def int(name: String): SegmentMatcher[Int] = new NamedArgSegmentMatcher(Path.int(name))

  def integer(name: String): SegmentMatcher[Integer] = new NamedArgSegmentMatcher(Path.integer(name))}
