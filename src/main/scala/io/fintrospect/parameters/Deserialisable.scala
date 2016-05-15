package io.fintrospect.parameters

trait Deserialisable[T] {
  val deserialize: Seq[String] => T
}
