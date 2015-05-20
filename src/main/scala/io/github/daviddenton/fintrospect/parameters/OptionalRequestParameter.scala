package io.github.daviddenton.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try


abstract class OptionalRequestParameter[T](location: Location, parse: (String => Try[T]))
  extends RequestParameter[T](Requirement.Optional, location, parse) {
  def from(request: HttpRequest): Option[T] = unapply(request)
}


object OptionalRequestParameter {
  def builderFor(location: Location) = new ParameterBuilder[OptionalRequestParameter]() {
    def apply[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Try[T])) =
      new OptionalRequestParameter[T](location, parse) {
        override val name = aName
        override val description = aDescription
        override val paramType = aParamType
      }
  }
}
