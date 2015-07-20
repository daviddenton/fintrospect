package io.fintrospect.parameters


trait Retrieval[T, From] {

  /**
   * Extract the parameter from the target object.
   */
  def <--(from: From): T

  /**
   * Extract the parameter from the target object.
   */
  def from(from: From): T = <--(from)
}

trait Mandatory[T,From] extends Retrieval[T, From] with Parameter with Validatable[T, From] {
  override val required = true
  def <--(from: From) = validate(from).right.get.get
}

trait Optional[T, From] extends Retrieval[Option[T], From] with Parameter with Validatable[T, From]  {
  override val required = false
  def <--(from: From) = validate(from).right.toOption.get
}
