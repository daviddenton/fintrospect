package io.fintrospect.parameters

import scala.util.Try

object OptionalRequestParameter {
  def builder(location: Location) = () => new ParameterBuilder[RequestParameter, Optional]() {
    def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new RequestParameter[T](name, description, paramType, location, parse) with Optional[T]
  }
}