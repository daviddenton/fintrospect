package io.github.daviddenton.fintrospect

import org.jboss.netty.handler.codec.http.HttpResponseStatus

case class PathResponse(code: HttpResponseStatus, description: String)