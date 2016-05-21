package io.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import io.fintrospect.parameters.PathParameter
import io.fintrospect.renderers.ModuleRenderer

abstract class ServerRoute[RQ, RS](val routeSpec: RouteSpec,
                                   val method: Method,
                                   pathFn: Path => Path,
                                   val pathParams: PathParameter[_]*) {

  def validationFilter(moduleRenderer: ModuleRenderer) = Filter.mk[Request, Response, Request, Response] {
    (request, svc) => {
      val missingOrFailed = routeSpec.requestParams.++(routeSpec.body).map(_.extract(request)).flatMap(_.invalid)
      if (missingOrFailed.isEmpty) svc(request) else Future.value(moduleRenderer.badRequest(missingOrFailed))
    }
  }

  def matches(actualMethod: Method, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)

  def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path): PartialFunction[(Method, Path), Service[Request, Response]]

  def describeFor(basePath: Path): String = (pathFn(basePath).toString +: pathParams.map(_.toString())).mkString("/")
}

