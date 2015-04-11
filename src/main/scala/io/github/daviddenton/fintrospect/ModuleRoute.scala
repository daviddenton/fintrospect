package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path

class ModuleRoute(val route: Route, val basePath: Path) {
  override def toString: String = route.describeFor(basePath)
}
