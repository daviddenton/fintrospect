package io.fintrospect.util

import java.net.URI

object PathSegmentEncoderDecoder {
  def encode(in: String): String = in.split("/").map(new URI("http", "localhost", _).getRawFragment).mkString("%2F")

  def decode(in: String): String = new URI("http://localhost/" + in).getPath.substring(1)
}
