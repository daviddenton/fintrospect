package io.github.daviddenton.fintrospect

import com.twitter.finagle.{Filter, Service}
import com.twitter.finagle.http.path.Path
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest, HttpMethod}

/**
 * These are the Finagle Types used throughout Fintrospect to aid readability.
 */
object FinagleTypeAliases {
  type FTRequest = HttpRequest
  type FTResponse = HttpResponse
  type FTService = Service[FTRequest, FTResponse]
  type FTFilter = Filter[FTRequest, FTResponse, FTRequest, FTResponse]
  type Binding = PartialFunction[(HttpMethod, Path), FTService]
}
