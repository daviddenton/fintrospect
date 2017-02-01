package io.fintrospect.parameters

case class MyCustomType(value: Int) extends AnyVal with Value[Int]
