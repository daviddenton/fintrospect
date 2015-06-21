package io.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.parameters.PathParameter
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

abstract class Route(val describedRoute: DescribedRoute, val method: HttpMethod, pathFn: Path => Path, val pathParams: PathParameter[_]*) {

  def missingOrFailedFrom(request: HttpRequest) = {
    val validatible = describedRoute.requestParams ++ describedRoute.body.map(_.iterator).getOrElse(Nil).toSeq
    val paramsAndParseResults = validatible.map(p => (p, p.attemptToParseFrom(request)))
    val withoutMissingOptionalParams = paramsAndParseResults.filterNot(pr => !pr._1.required && pr._2.isEmpty)
    withoutMissingOptionalParams.filterNot(pr => pr._2.isDefined && pr._2.get.isSuccess).map(_._1)
  }

  def matches(actualMethod: HttpMethod, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)

  def toPf(basePath: Path): Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse] => PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]

  def describeFor(basePath: Path): String = (pathFn(basePath).toString :: pathParams.map(_.toString()).toList).mkString("/")
}
