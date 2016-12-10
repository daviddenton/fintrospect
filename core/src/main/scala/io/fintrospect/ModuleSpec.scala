package io.fintrospect

import com.twitter.finagle.Filter
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.renderers.ModuleRenderer

/**
  * Renamed to RouteModule.
  * Self-describing module builder (uses the immutable builder pattern).
  */
@deprecated("Renamed: use RouteModule instead", "13.16.0")
object ModuleSpec {
  @deprecated("Renamed: use RouteModule instead", "13.16.0")
  def apply(basePath: Path): RouteModule[Request, Response] = RouteModule(basePath)

  @deprecated("Renamed: use RouteModule instead", "13.16.0")
  def apply(basePath: Path, moduleRenderer: ModuleRenderer): RouteModule[Request, Response] = RouteModule(basePath, moduleRenderer)

  @deprecated("Renamed: use RouteModule instead", "13.16.0")
  def apply[RQ, RS](basePath: Path, moduleRenderer: ModuleRenderer, moduleFilter: Filter[Request, Response, RQ, RS]): RouteModule[RQ, RS] =
    RouteModule(basePath, moduleRenderer, moduleFilter)
}
