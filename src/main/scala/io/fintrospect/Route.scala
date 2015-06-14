package io.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.parameters.{ParseableParameter, PathParameter}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

abstract class Route(val describedRoute: DescribedRoute, val method: HttpMethod, pathFn: Path => Path, val pathParams: PathParameter[_]*) {

  def validatible: Seq[ParseableParameter[_]] = {
    describedRoute.requestParams ++ describedRoute.body.map(_.iterator).getOrElse(Nil).toSeq
  }

  def matches(actualMethod: HttpMethod, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)

  def toPf(basePath: Path): Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse] => PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]

  def describeFor(basePath: Path): String = (pathFn(basePath).toString :: pathParams.map(_.toString()).toList).mkString("/")
}
