package io.fintrospect.parameters


/**
 * By implementing ParameterSpecSupplier[T], we can declare parameters using a shorthand: eg. Path(HipsterBeardStyle)
 */
trait ParameterSpecSupplier[T] {
  def spec: ParameterSpec[T]
}
