package io.fintrospect.parameters

/**
  * Represents the ability to retrieve a parameter value from an enclosing object (request/form etc..)
  */
trait Retrieval[T, -From] {

  /**
    * Extract the parameter from the target object. Throws on failure, but that shouldn't be a problem as the pre-validation
    * stage for declared parameters and bodies handles the failure before user code is entered.
    */
  def <--(from: From): T

  /**
    * Extract the parameter from the target object. Throws on failure, but that shouldn't be a problem as the pre-validation
    * stage for declared parameters and bodies handles the failure before user code is entered.
    */
  def from(from: From): T = <--(from)
}

trait Mandatory[T, From] extends Retrieval[T, From] with Parameter with Validatable[T, From] {
  override val required = true

  protected def extract(from: From): Extraction[T]

  override def <--?(from: From): Extraction[T] = extract(from).map(identity)

  override def <--(from: From): T = validate(from).asRight.get.get
}

trait Optional[T, From] extends Retrieval[Option[T], From] with Parameter with Validatable[Option[T], From] {
  override val required = false

  protected def extract(from: From): Extraction[T]

  override def <--?(from: From) = extract(from).map(Some(_))

  def <--(from: From): Option[T] = validate(from).asRight.get.flatMap(i => i)
}
