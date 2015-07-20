package io.fintrospect.parameters

import argo.jdom.JsonRootNode

trait BodyParameter extends Parameter {
  val example: Option[JsonRootNode]
}
