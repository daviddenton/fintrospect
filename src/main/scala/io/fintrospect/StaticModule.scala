package io.fintrospect

import com.twitter.finagle.http.Response
import com.twitter.finagle.http.path.Path
import io.fintrospect.Types._

class StaticModule(basePath: Path, moduleFilter: FFilter[Response]) extends Module {
  override protected def serviceBinding: ServiceBinding = ???
}
