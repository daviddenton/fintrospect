package io.fintrospect.parameters

trait ParseableParameter[T, From] extends Parameter[T] {
  def validate(from: From): Either[Parameter[_], Option[T]]
}
