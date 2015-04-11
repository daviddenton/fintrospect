package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.{Parameter, Requirement}

class ModuleRoute2(val description: Description, completedPath: CompletePath, basePath: Path) {
  val allParams: List[(Requirement, Parameter[_])] = {
    (description.params ++ completedPath.params.flatMap(identity)).map(p => p.requirement -> p)
  }

  override def toString: String = completedPath.describeFor(basePath)
}
