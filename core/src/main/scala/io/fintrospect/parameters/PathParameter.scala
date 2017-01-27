package io.fintrospect.parameters

abstract class PathParameter[T](val name: String, val description: String = null, spec: ParameterSpec[_], val isFixed: Boolean) extends Parameter with Iterable[PathParameter[_]] {
  override val paramType = spec.paramType
  override val where = "path"

  def unapply(str: String): Option[T]
}
