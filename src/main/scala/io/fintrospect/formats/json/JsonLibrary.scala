package io.fintrospect.formats.json

/**
 * Defines a supported JSON library format (e.g. Argo or Json4s)
 * @tparam R - Root node type
 * @tparam N - Node type
 */
trait JsonLibrary[R, N] {

  /**
   * Use this to parse and create JSON objects in a generic way
   */
  val JsonFormat: JsonFormat[R, N]

  /**
   * Use this to create JSON-format HttpResponses
   */
  object ResponseBuilder extends JsonResponseBuilder[R, N](JsonFormat)
}
