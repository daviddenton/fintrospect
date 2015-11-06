package io.fintrospect.parameters

trait ParameterSpecSupplier[T] {
  def spec: ParameterSpec[T]
}
