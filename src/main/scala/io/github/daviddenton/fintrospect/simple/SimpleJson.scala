package io.github.daviddenton.fintrospect.simple

import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.ModuleRoute

object SimpleJson extends (Seq[ModuleRoute] => JsonRootNode) {
//  def apply(moduleRoutes: Seq[ModuleRoute]): JsonRootNode = obj("resources" -> obj(moduleRoutes.map(_.describe)))
  def apply(moduleRoutes: Seq[ModuleRoute]): JsonRootNode = ???
}
