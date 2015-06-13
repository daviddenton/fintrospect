package io.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.parameters.{Parameter, PathParameter}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

abstract class Route(val describedRoute: DescribedRoute, val method: HttpMethod, pathFn: Path => Path, val pathParams: PathParameter[_]*) {

  val allParams: List[Parameter[_]] = describedRoute.params ++ pathParams.flatMap(identity)

  def matches(actualMethod: HttpMethod, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)

  def toPf(basePath: Path): Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse] => PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]

  def describeFor(basePath: Path): String = (pathFn(basePath).toString :: pathParams.map(_.toString()).toList).mkString("/")
}
