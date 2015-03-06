package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path

case class ModuleRoute(description: Description, rootPath: Path, segmentMatchers: Seq[SegmentMatcher[_]]) {
  override def toString: String = (description.complete(rootPath).toString :: segmentMatchers.map(_.toString).toList).mkString("/")
}
