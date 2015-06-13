package io.fintrospect.parameters

/**
 * Spec required to marshall a parameter of a custom type
 * @param deserialize function to take the input string from the request and attempt to construct a deserialized instance of T. Exceptions are
 *                    automatically caught and translated into the appropriate result, so just concentrate on the Happy-path case
 * @param serialize function to take the input type and serialize it to a string to be represented in the request
 * @param description optional description of the parameter (for use in description endpoints)
 * @tparam T the type of the parameter
 * @return a parameter for retrieving a value of type [T] from the request
 */
case class ParameterSpec[T](name: String,
                            description: Option[String] = None,
                            paramType: ParamType,
                            deserialize: String => T,
                            serialize: T => String)
