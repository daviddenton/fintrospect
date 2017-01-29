package io.fintrospect.parameters

case class MyCustomType(value: Int)

object MyCustomType extends ParameterSpecSupplier[MyCustomType] {
  override def spec = ParameterSpec.int().map(s => MyCustomType(s), ct => ct.value)
}
