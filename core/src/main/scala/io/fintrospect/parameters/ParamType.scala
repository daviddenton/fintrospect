package io.fintrospect.parameters

/**
 * How a parameter is represented in the HTTP message (JSON type)
 */
sealed class ParamType(val name: String)

object StringParamType extends ParamType("string")
object ArrayParamType extends ParamType("array")
object NumberParamType extends ParamType("number")
object IntegerParamType extends ParamType("integer")
object ObjectParamType extends ParamType("object")
object BooleanParamType extends ParamType("boolean")
object NullParamType extends ParamType("null")
