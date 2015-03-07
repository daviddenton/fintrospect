package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.Locations.Path
import org.joda.time.DateTime

object SegmentMatchers {

  def fixed(value: String): SegmentMatcher[String] = new SegmentMatcher[String] {
    override def unapply(str: String): Option[String] = if (str == value) Some(str) else None
    override def toString: String = value
  }

  def dateTime(name: String): SegmentMatcher[DateTime] = new NamedArgSegmentMatcher(Parameters.dateTime(Path, name))

  def boolean(name: String): SegmentMatcher[Boolean] = new NamedArgSegmentMatcher(Parameters.boolean(Path, name))

  def string(name: String): SegmentMatcher[String] = new NamedArgSegmentMatcher(Parameters.string(Path, name))

  def long(name: String): SegmentMatcher[Long] = new NamedArgSegmentMatcher(Parameters.long(Path, name))

  def int(name: String): SegmentMatcher[Int] = new NamedArgSegmentMatcher(Parameters.int(Path, name))

  def integer(name: String): SegmentMatcher[Integer] = new NamedArgSegmentMatcher(Parameters.integer(Path, name))}
