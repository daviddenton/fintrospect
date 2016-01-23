package io.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}
import io.fintrospect.Aliases._
import io.fintrospect.parameters.PathParameter

abstract class ServerRoute[RS](val routeSpec: RouteSpec,
                           val method: Method,
                           pathFn: Path => Path,
                           val pathParams: PathParameter[_]*) {

  def missingOrFailedFrom(request: Request) = {
    val validations = routeSpec.headerParams.map(_.validate(request)) ++
      routeSpec.queryParams.map(_.validate(request)) ++
      routeSpec.body.toSeq.flatMap(_.validate(request))
    validations.collect { case Left(l) => l }
  }

  def matches(actualMethod: Method, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)

  def toPf(filter: Filter[RS], basePath: Path): Filter[Response] => SvcBinding

  def describeFor(basePath: Path): String = (pathFn(basePath).toString +: pathParams.map(_.toString())).mkString("/")
}

