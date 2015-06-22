package io.fintrospect.parameters

import java.net.URI

import scala.util.Try

trait Marker[T]

object Path extends Parameters[PathParameter, Marker] {

  /**
   * A special path segment that is defined, but has no intrinsic value other than for route matching. Useful when embedded
   * between 2 other path parameters. eg. /myRoute/{id}/aFixedPart/{subId}
   */
  def fixed(name: String): PathParameter[String] = new PathParameter[String](ParameterSpec(name, None, StringParamType, identity, identity)) {

    override val required = true

    override def toString() = name

    override def ->(unused: String) = ParamBinding(this, name)

    override def unapply(str: String) = if (str == name) Option(str) else None

    override def iterator = Nil.iterator
  }

  /**
   * Create a path parameter using the passed specification
   * @param spec the parameter spec
   * @tparam T the type of the parameter
   * @return a parameter for retrieving a value of type [T] from the request
   */
  def apply[T](spec: ParameterSpec[T]) = new PathParameter[T](spec) with Marker[T] {

    override val required = true

    override def toString() = s"{$name}"

    override def ->(value: T) = ParamBinding[T](this, spec.serialize(value))

    override def unapply(str: String) = Option(str).flatMap(s => {
      Try(spec.deserialize(new URI("http://localhost/" + s).getPath.substring(1))).toOption
    })

    override def iterator = Option(this).iterator
  }
}
