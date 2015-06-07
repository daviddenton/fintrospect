package io.fintrospect.parameters

import scala.util.Try

object RequiredRequestParameter {
  def builder(location: Location) = () => new ParameterBuilder[RequestParameter, Mandatory]() {
    def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new RequestParameter[T](name, description, paramType, location, parse) with Mandatory[T]
  }
}