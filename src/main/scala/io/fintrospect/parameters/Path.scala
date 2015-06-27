package io.fintrospect.parameters

import java.net.URI

import scala.util.Try

trait PathBindable[T] extends Bindable[T, PathBinding]

object Path extends Parameters[PathParameter, PathBindable] {

  /**
   * A special path segment that is defined, but has no intrinsic value other than for route matching. Useful when embedded
   * between 2 other path parameters. eg. /myRoute/{id}/aFixedPart/{subId}
   */
  def fixed(name: String): PathParameter[String] = new PathParameter[String](
    ParameterSpec(name, None, StringParamType, identity, identity), true) with PathBindable[String] {

    override val required = true

    override def toString() = name

    override def -->(unused: String) = Seq()

    override def unapply(str: String) = if (str == name) Option(str) else None

    override def iterator = Nil.iterator
  }

  /**
   * Create a path parameter using the passed specification
   * @param spec the parameter spec
   * @tparam T the type of the parameter
   * @return a parameter for retrieving a value of type [T] from the request
   */
  def apply[T](spec: ParameterSpec[T]) = new PathParameter[T](spec, false) with PathBindable[T] {

    override val required = true

    override def toString() = s"{$name}"

    override def unapply(str: String) = Option(str).flatMap(s => {
      Try(spec.deserialize(new URI("http://localhost/" + s).getPath.substring(1))).toOption
    })

    override def -->(value: T) = Seq(new PathBinding(this, spec.serialize(value)))

    override def iterator = Option(this).iterator
  }
}
