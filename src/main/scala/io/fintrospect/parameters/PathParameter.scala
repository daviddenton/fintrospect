package io.fintrospect.parameters

abstract class PathParameter[T](spec: ParameterSpec[T]) extends Parameter[T] with Iterable[PathParameter[_]] {
  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType
  override val where = "path"
  def unapply(str: String): Option[T]

  override def ->(value: T) = PathBinding(this, spec.serialize(value))

}
