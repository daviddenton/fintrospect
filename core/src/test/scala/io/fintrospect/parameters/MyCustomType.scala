package io.fintrospect.parameters

case class MyCustomType(value: Int)

object MyCustomType extends ParameterSpecSupplier[MyCustomType] {
  override def spec = ParameterSpec.string("name").map(s => MyCustomType(s.toInt), ct => ct.value.toString)
}
