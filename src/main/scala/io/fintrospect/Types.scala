package io.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}

/**
  * Some type aliases that are used throughout Fintrospect
  */
object Types {
  type ServiceBinding = PartialFunction[(Method, Path), Service[Request, Response]]
  type FFilter[RS] = Filter[Request, Response, Request, RS]
}
