package io.fintrospect.parameters

trait Validatable[T, From] extends Parameter[T] {
  def validate(from: From): Either[Parameter[_], Option[T]]
}
