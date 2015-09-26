package io.fintrospect.util.json

/**
 * Defines a type of JSON format (e.g. Argo or Json4s)
 * @tparam R - Root node type
 * @tparam N - Node type
 * @tparam F - Field type
 */
trait JsonLibrary[R, N, F] {

  /**
   * Use this to parse and create JSON objects in a generic way
   */
  val JsonFormat: JsonFormat[R, N, F]

  val ResponseBuilder = new JsonResponseBuilder[R, N, F](JsonFormat)
}
