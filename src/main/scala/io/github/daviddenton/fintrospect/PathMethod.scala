package io.github.daviddenton.fintrospect

import org.jboss.netty.handler.codec.http.HttpMethod

case class PathMethod(method: HttpMethod, summary: String, params: Seq[Parameter], responses: Seq[PathResponse], securities: Seq[Security])