package io.github.daviddenton.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest


class OptionalRequestParameter[T](name: String, description: Option[String], location: Location, paramType: ParamType, parse: (String => Option[T]))
  extends RequestParameter[T](name, description, location, paramType, parse) {
  def from(request: HttpRequest): Option[T] = unapply(request)
  override val requirement = Requirement.Optional
}


object OptionalRequestParameter {
  def builderFor(location: Location) = new ParameterBuilder[OptionalRequestParameter]() {
    def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Option[T])) = new OptionalRequestParameter[T](name, description, location, paramType, parse)
  }
}
