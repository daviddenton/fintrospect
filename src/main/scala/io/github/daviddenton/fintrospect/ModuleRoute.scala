package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.SegmentMatcher

class ModuleRoute(val description: Description, rootPath: Path, segmentMatchers: Seq[SegmentMatcher[_]]) {
  val params = segmentMatchers.flatMap(_.toParameter) ++ description.params

  override def toString: String = (description.complete(rootPath).toString :: segmentMatchers.map(_.toString).toList).mkString("/")
}
