package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path

private[fintrospect] class ModuleRoute[D <: Description] protected[fintrospect](description: D, rootPath: Path, segmentMatchers: Seq[SegmentMatcher[_]]) {
  def describe = description.toJsonField(rootPath, segmentMatchers)

  override def toString: String = (description.complete(rootPath).toString :: segmentMatchers.map(_.toString).toList).mkString("/")
}
