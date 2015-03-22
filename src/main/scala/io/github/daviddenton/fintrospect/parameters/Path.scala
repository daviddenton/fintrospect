package io.github.daviddenton.fintrospect.parameters

object Path extends Parameters[PathParameter](PathParameter.builder) {
  
  def fixed(name: String): PathParameter[String] = new PathParameter[String](name, None) {
    override def toString() = name

    override def unapply(str: String): Option[String] = if (str == name) Some(str) else None

    override def iterator: Iterator[PathParameter[_]] = Nil.iterator
  }
}
