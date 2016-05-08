package io.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.parameters.PathParameter

abstract class ServerRoute[RQ, RS](val routeSpec: RouteSpec,
                                   val method: Method,
                                   pathFn: Path => Path,
                                   val pathParams: PathParameter[_]*) {

  def missingOrFailedFrom(request: Request) = {
    val validations = routeSpec.requestParams.map(_.validate(request)) ++
      routeSpec.body.toSeq.map(_.validate(request))
    validations.flatMap(_.invalid)
  }

  def matches(actualMethod: Method, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)

  def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path): PartialFunction[(Method, Path), Service[Request, Response]]

  def describeFor(basePath: Path): String = (pathFn(basePath).toString +: pathParams.map(_.toString())).mkString("/")
}

