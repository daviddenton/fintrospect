package examples.customformats

import io.fintrospect.parameters.{ParameterSpec, ParameterSpecSupplier, StringParamType}

case class HipsterBeardStyle(name: String)

/**
 * By implementing ParameterSpecSupplier, we can declare parameters using the shorthand: Path(HipsterBeardStyle)
 */
object HipsterBeardStyle extends ParameterSpecSupplier[HipsterBeardStyle] {
  override val spec = ParameterSpec[HipsterBeardStyle]("beardStyle", None, StringParamType, HipsterBeardStyle(_), _.name)
}