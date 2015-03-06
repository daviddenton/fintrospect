package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.Location.Location

case class Parameter(name: String, location: Location, paramType: String)