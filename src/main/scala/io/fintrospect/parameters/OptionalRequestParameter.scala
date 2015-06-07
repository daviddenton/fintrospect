package io.fintrospect.parameters

import scala.util.Try

object OptionalRequestParameter {
  def builder(location: Location) = () => new ParameterBuilder[RequestParameter, Optional]() {
    def apply[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Try[T])) =
      new RequestParameter[T](location, parse) with Optional[T] {
        override val name = aName
        override val description = aDescription
        override val paramType = aParamType
      }
  }
}