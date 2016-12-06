package io.fintrospect.parameters

import com.twitter.io.Buf

case class MultiPartFile(content: Buf, contentType: Option[String] = None, filename: Option[String] = None)
