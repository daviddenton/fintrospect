package io.fintrospect
import com.twitter.finagle.{Filter => FinFilter, Service}
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}

object Aliases {
  type Filter[T] = FinFilter[Request, Response, Request, T]
  type SvcBinding = PartialFunction[(Method, Path), Service[Request, Response]]
}
