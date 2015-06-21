package io.fintrospect.parameters

trait Validatable[T, From] {
  def validate(from: From): Either[Parameter[_], Option[T]]
}
