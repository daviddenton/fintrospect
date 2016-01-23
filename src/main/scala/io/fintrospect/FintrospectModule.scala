package io.fintrospect

import com.twitter.finagle.http.Response
import com.twitter.finagle.http.path.Path
import io.fintrospect.Types._
import io.fintrospect.renderers.ModuleRenderer

@deprecated("Use ModuleSpec instead", "v12.0.0")
object FintrospectModule {
  @deprecated("Use ModuleSpec() instead", "v12.0.0")
  def apply(basePath: Path, moduleRenderer: ModuleRenderer): ModuleSpec[Response] =
    ModuleSpec(basePath, moduleRenderer)

  @deprecated("Use ModuleSpec() instead", "v12.0.0")
  def apply(basePath: Path, moduleRenderer: ModuleRenderer, moduleFilter: FFilter[Response]) = {
    ModuleSpec(basePath, moduleRenderer, moduleFilter)
  }
}
