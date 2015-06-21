package io.fintrospect.parameters

import java.net.URI

import scala.util.Try

object Path {

  /**
   * A special path segment that is defined, but has no intrinsic value other than for route matching. Useful when embedded
   * between 2 other path parameters. eg. /myRoute/{id}/aFixedPart/{subId}
   */
  def fixed(name: String): PathParameter[String] = new PathParameter[String](ParameterSpec(name, None, StringParamType, identity, identity)) {

    override val required = true

    override def toString() = name

    override def ->(value: String): ParamBinding[String] = ParamBinding(this, value)

    override def unapply(str: String): Option[String] = if (str == name) Some(str) else None

    override def iterator: Iterator[PathParameter[_]] = Nil.iterator
  }

  /**
   * Create a path parameter using the passed specification
   * @param spec the parameter spec
   * @tparam T the type of the parameter
   * @return a parameter for retrieving a value of type [T] from the request
   */
  def apply[T](spec: ParameterSpec[T]) = new PathParameter[T](spec) {

    override val required = true

    override def toString() = s"{$name}"

    override def ->(value: T): ParamBinding[T] = ParamBinding[T](this.asInstanceOf[Parameter[T]], spec.serialize(value))

    override def unapply(str: String): Option[T] = Option(str).flatMap(s => {
      Try(spec.deserialize(new URI("http://localhost/" + s).getPath.substring(1))).toOption
    })

    override def iterator: Iterator[PathParameter[_]] = Some(this).iterator
  }
}
