package io.github.daviddenton.fintrospect.simple

import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.ModuleRoute
import io.github.daviddenton.fintrospect.util.ArgoUtil._

object SimpleJson extends (Seq[ModuleRoute[SimpleDescription]] => JsonRootNode) {
  def apply(moduleRoutes: Seq[ModuleRoute[SimpleDescription]]): JsonRootNode = obj("resources" -> obj(moduleRoutes.map(_.describe)))
}
