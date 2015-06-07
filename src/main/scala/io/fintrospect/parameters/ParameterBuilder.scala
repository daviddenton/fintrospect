package io.fintrospect.parameters

import scala.language.higherKinds
import scala.util.Try

trait ParameterBuilder[P[_], Require[_]] {
  def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])): P[T] with Require[T]
}
