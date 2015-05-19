package io.github.daviddenton.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

abstract class RequiredRequestParameter[T](location: Location, parse: (String => Option[T])) 
  extends RequestParameter[T](Requirement.Mandatory, location, parse) {
  def from(request: HttpRequest): T = unapply(request).get
}

object RequiredRequestParameter {
  def builderFor(location: Location) = new ParameterBuilder[RequiredRequestParameter]() {
    override def apply[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Option[T])) =
      new RequiredRequestParameter[T](location, parse) {
        override val name = aName
        override val description = aDescription
        override val paramType = aParamType
      }
  }
}