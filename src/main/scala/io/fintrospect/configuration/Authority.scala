package io.fintrospect.configuration

import scala.util.Try

case class Authority(host: Host, port: Port) {
  override def toString: String = s"${host.value}:${port.value}"
}

object Authority {
  def unapply(str: String): Option[Authority] = {
    val parts = str.split(":")
    parts.length match {
      case 1 => Try(Host(parts(0)).toAuthority(Port(80))).toOption
      case 2 => Try(Host(parts(0)).toAuthority(Port(parts(1)))).toOption
      case _ => None
    }
  }
}

