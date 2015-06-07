package io.fintrospect.parameters

import scala.util.Try

object RequiredRequestParameter {
  def builder(location: Location) = () => new ParameterBuilder[RequestParameter, Mandatory]() {
    def apply[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Try[T])) =
      new RequestParameter[T](location, parse) with Mandatory[T] {
        override val name = aName
        override val description = aDescription
        override val paramType = aParamType
      }
  }
}