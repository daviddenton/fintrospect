package io.fintrospect.configuration

case class Port(value: Int) extends AnyVal {
  override def toString = value.toString
}

object Port {
  def apply(value: String): Port = Port(value.toInt)
}



