package io.github.daviddenton.fintrospect.swagger

import io.github.daviddenton.fintrospect.swagger.Location.Location

case class SwParameter(name: String, location: Location, paramType: String)