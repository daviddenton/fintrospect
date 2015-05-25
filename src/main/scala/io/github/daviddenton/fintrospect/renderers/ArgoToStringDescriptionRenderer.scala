package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Route
import io.github.daviddenton.fintrospect.util.ArgoUtil

class ArgoToStringDescriptionRenderer(delegate: DescriptionRenderer[JsonRootNode]) extends DescriptionRenderer[String] {
  override def apply(basePath: Path, routes: Seq[Route]): String = ArgoUtil.pretty(delegate(basePath, routes))
}
