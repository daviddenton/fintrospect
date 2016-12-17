package examples.customformats

import io.fintrospect.parameters.{ParameterSpec, ParameterSpecSupplier}

case class HipsterBeardStyle(name: String)

/**
  * By implementing ParameterSpecSupplier, we can declare parameters using the shorthand: Path(HipsterBeardStyle)
  */
object HipsterBeardStyle extends ParameterSpecSupplier[HipsterBeardStyle] {
  override val spec = ParameterSpec.string("beardStyle").map(i => HipsterBeardStyle(i), (h: HipsterBeardStyle) => h.name)
}