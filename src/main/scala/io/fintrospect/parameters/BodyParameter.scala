package io.fintrospect.parameters

import argo.jdom.JsonRootNode

trait BodyParameter[T] extends Parameter[T] {
  val example: Option[JsonRootNode]
}
