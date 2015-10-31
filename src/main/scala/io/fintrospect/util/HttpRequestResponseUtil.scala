package io.fintrospect.util

import com.twitter.finagle.http.{Message, Response, Status}

object HttpRequestResponseUtil {
  def contentFrom(msg: Message): String = msg.contentString
  def statusAndContentFrom(msg: Response): (Status, String) = (msg.status, contentFrom(msg))

  def headersFrom(msg: Message) : Map[String, String] = {
    Map(msg.headerMap.map(entry => entry._1 -> entry._2).toSeq: _*)
  }
}
