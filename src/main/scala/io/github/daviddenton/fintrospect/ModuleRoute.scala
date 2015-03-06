package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.swagger.SwDescription

private[fintrospect] class ModuleRoute protected[fintrospect](description: SwDescription, rootPath: Path, segmentMatchers: Seq[SegmentMatcher[_]]) {
  override def toString: String = (description.complete(rootPath).toString :: segmentMatchers.map(_.toString).toList).mkString("/")
}
