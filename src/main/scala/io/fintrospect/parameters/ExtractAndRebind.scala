package io.fintrospect.parameters

trait ExtractAndRebind[From, B <: Binding] {
  def newBinding(parameter: Parameter, value: String): B
  def valuesFrom(parameter: Parameter, from: From): Option[Seq[String]]
}
