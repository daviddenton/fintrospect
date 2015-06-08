package io.fintrospect.parameters

import scala.util.Try

abstract class NonBodyRequestParameter[T](name: String,
                                          description: Option[String],
                                          paramType: ParamType,
                                          location: Location,
                                          parse: (String => Try[T]))
  extends RequestParameter[T](name, description, paramType, location, parse)
