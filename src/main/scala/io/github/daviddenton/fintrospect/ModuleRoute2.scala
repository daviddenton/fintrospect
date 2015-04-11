package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.{Parameter, Requirement}

class ModuleRoute2(val completedPath: CompletePath, val basePath: Path) {
  val description: Description = completedPath.description
  val allParams: List[(Requirement, Parameter[_])] = completedPath.allParams
  override def toString: String = completedPath.describeFor(basePath)
}
