package io.fintrospect.configuration

import java.net.InetSocketAddress

case class Host(value: String) extends AnyVal {

  override def toString = value

  def toAuthority(port: Port) = Authority(this, port)

  def socketAddress(port: Port) = new InetSocketAddress(value, port.value)
}

object Host {
  val localhost = Host("localhost")
}