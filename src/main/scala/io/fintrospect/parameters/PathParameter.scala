package io.fintrospect.parameters

abstract class PathParameter[T](val name: String, val description: Option[String], val paramType: ParamType) extends Parameter[T] with Iterable[PathParameter[_]] {
  override val where = "path"
  override val required = true
  def unapply(str: String): Option[T]
}
