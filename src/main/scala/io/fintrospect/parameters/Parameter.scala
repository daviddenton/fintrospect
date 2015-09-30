package io.fintrospect.parameters

/**
 * A parameter is a name-value pair which can be encoded into an HTTP message. Sub-types
 * represent the various places in which values are encoded (eg. header/form/query/path)
 */
trait Parameter {
  val required: Boolean
  val name: String
  val description: Option[String]
  val where: String
  val paramType: ParamType

  override def toString = s"Parameter(name=$name,where=$where,paramType=${paramType.name})"
}


