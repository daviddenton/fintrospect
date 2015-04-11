package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.{Parameter, Requirement}

class ModuleRoute(val route: Route, val basePath: Path) {
  val description: Description = route.description
  val allParams: List[(Requirement, Parameter[_])] = route.allParams
  override def toString: String = route.describeFor(basePath)
}
