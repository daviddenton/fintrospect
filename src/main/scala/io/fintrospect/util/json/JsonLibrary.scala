package io.fintrospect.util.json

/**
 * Defines a type of JSON format (e.g. Argo or Json4s)
 * @tparam R - Root node type
 * @tparam N - Node type
 */
trait JsonLibrary[R, N] {

  /**
   * Use this to parse and create JSON objects in a generic way
   */
  val JsonFormat: JsonFormat[R, N]

  val ResponseBuilder = new JsonResponseBuilder[R, N](JsonFormat)
}
