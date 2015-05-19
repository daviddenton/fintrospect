package io.github.daviddenton.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest


abstract class OptionalRequestParameter[T](location: Location, parse: (String => Option[T]))
  extends RequestParameter[T](Requirement.Optional, location, parse) {
  def from(request: HttpRequest): Option[T] = unapply(request)
}


object OptionalRequestParameter {
  def builderFor(location: Location) = new ParameterBuilder[OptionalRequestParameter]() {
    def apply[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Option[T])) =
      new OptionalRequestParameter[T](location, parse) {
        override val name = aName
        override val description = aDescription
        override val paramType = aParamType
      }
  }
}
