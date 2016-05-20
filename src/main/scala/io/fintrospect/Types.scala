package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}

/**
  * Some type aliases that are used throughout Fintrospect
  */
object types {
  type ServiceBinding = PartialFunction[(Method, Path), Service[Request, Response]]
}
