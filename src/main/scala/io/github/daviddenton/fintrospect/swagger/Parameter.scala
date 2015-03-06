package io.github.daviddenton.fintrospect.swagger

import io.github.daviddenton.fintrospect.Location
import Location.Location

case class Parameter(name: String, location: Location, paramType: String)