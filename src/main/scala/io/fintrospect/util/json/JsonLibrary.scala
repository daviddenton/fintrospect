package io.fintrospect.util.json

trait JsonLibrary[R, N, F] {
  val JsonFormat: JsonFormat[R, N, F]

  val ResponseBuilder = new JsonResponseBuilder[R, N, F](JsonFormat)
}
