package examples.customformats

import io.fintrospect.parameters.Value

case class HipsterBeardStyle(value: String) extends AnyVal with Value[String]
