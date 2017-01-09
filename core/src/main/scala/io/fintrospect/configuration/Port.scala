package io.fintrospect.configuration

case class Port(value: Int) extends AnyVal {
  override def toString = value.toString
}

object Port {
  val _80 = Port(80)
  val _443 = Port(443)

  def apply(value: String): Port = Port(value.toInt)
}



