package io.github.daviddenton.fintrospect.parameters

/**
 * Builder for parameters that are encoded in the HTTP request path.
 */
object Path extends Parameters[PathParameter](PathParameter.builder) {

  /**
   * A special path segment that is defined, but has no intrinsic value other than for route matching. Usefull when embedded
   * between 2 other path parameters. eg. /myRoute/{id}/aFixedPart/{subId}
   */
  def fixed(aName: String): PathParameter[String] = new PathParameter[String] {
    override val name = aName
    override val description = None
    override val paramType = StringParamType

    override def toString() = name

    override def unapply(str: String): Option[String] = if (str == name) Some(str) else None

    override def iterator: Iterator[PathParameter[_]] = Nil.iterator
  }
}
