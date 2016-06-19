package io.fintrospect.util

import io.fintrospect.filters.{RequestFilters, ResponseFilters}

/**
  * General case useful filters
  */
@deprecated("use filters in io.fintrospect.filters package instead", "v12.21.0")
object Filters {

  @deprecated("use filters in io.fintrospect.filters.RequestFilters instead", "v12.21.0")
  val Request = RequestFilters

  @deprecated("use filters in io.fintrospect.filters.ResponseFilters instead", "v12.21.0")
  val Response = ResponseFilters

}
