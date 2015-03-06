package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.swagger.Description

case class ModuleRoute protected[fintrospect](description: Description, rootPath: Path, segmentMatchers: Seq[SegmentMatcher[_]]) {
  override def toString: String = (description.complete(rootPath).toString :: segmentMatchers.map(_.toString).toList).mkString("/")
}
