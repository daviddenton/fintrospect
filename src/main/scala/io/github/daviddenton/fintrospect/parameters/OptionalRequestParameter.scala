package io.github.daviddenton.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest


class OptionalRequestParameter[T](override val name: String,
                                  override val description: Option[String],
                                  location: Location,
                                  override val paramType: ParamType,
                                  parse: (String => Option[T]))
  extends RequestParameter[T](location, parse) {
  override val requirement = Requirement.Optional

  def from(request: HttpRequest): Option[T] = unapply(request)
}


object OptionalRequestParameter {
  def builderFor(location: Location) = new ParameterBuilder[OptionalRequestParameter]() {
    def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Option[T])) = new OptionalRequestParameter[T](name, description, location, paramType, parse)
  }
}
