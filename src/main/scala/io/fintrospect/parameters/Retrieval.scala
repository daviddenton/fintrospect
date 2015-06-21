package io.fintrospect.parameters


trait Retrieval[T, From] {
  def from(from: From): T
}

trait Mandatory[T,From] extends Retrieval[T, From] with Parameter[T] with Validatable[T, From] {
  override val required = true
  def from(from: From): T = validate(from).right.get.get
}

trait Optional[T, From] extends Retrieval[Option[T], From] with Parameter[T] with Validatable[T, From]  {
  override val required = false
  def from(from: From): Option[T] = {
    validate(from).right.toOption.get
  }
}
