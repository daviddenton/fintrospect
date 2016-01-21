package io.fintrospect

import com.twitter.finagle.Filter
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.renderers.ModuleRenderer

@deprecated("Use ModuleSpec instead", "v12.0.0")
object FintrospectModule {
  @deprecated("Use ModuleSpec() instead", "v12.0.0")
  def apply(basePath: Path, moduleRenderer: ModuleRenderer): ModuleSpec =
    ModuleSpec(basePath, moduleRenderer)

  @deprecated("Use ModuleSpec() instead", "v12.0.0")
  def apply(basePath: Path, moduleRenderer: ModuleRenderer, moduleFilter: Filter[Request, Response, Request, Response]) = {
    ModuleSpec(basePath, moduleRenderer, moduleFilter)
  }
}
