package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.swagger.{Parameter, Response, SwSecurity}
import org.jboss.netty.handler.codec.http.HttpMethod

case class PathMethod(method: HttpMethod, summary: String, params: Seq[Parameter], responses: Seq[Response], securities: Seq[SwSecurity])