package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class RequiredRequestParameter[T](name: String, description: Option[String], location: Location, paramType: ParamType, parse: (String => Option[T]))
  extends RequestParameter[T](name, description, location, paramType, parse) {
  override val requirement = Requirement.Mandatory

  def from(request: Request): T = unapply(request).get
}

object RequiredRequestParameter {
  def builderFor(location: Location) = new ParameterBuilder[RequiredRequestParameter]() {
    override def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Option[T])) = new RequiredRequestParameter[T](name, description, location, paramType, parse)
  }
}