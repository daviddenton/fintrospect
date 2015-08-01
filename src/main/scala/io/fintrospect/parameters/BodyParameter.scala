package io.fintrospect.parameters

trait BodyParameter extends Parameter {
  val example: Option[String]
}
