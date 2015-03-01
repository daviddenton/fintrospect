package io.github.daviddenton.fintrospect.swagger2dot0

import argo.jdom.JsonNode
import io.github.daviddenton.fintrospect.util.ArgoUtil._

case class SwModel(private val o: Any) {
  protected[swagger2dot0] def toJsonPair: (String, JsonNode) = o.getClass.getSimpleName -> obj()
}

