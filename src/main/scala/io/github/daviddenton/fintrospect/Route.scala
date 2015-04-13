package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.FinagleTypeAliases.{Filter, Binding}
import io.github.daviddenton.fintrospect.parameters.{Parameter, PathParameter, Requirement}
import org.jboss.netty.handler.codec.http.HttpMethod

abstract class Route(val describedRoute: DescribedRoute, val method: HttpMethod, pathFn: Path => Path, val pathParams: PathParameter[_]*) {

  val allParams: List[(Requirement, Parameter[_])] = {
    (describedRoute.params ++ pathParams.flatMap(identity)).map(p => p.requirement -> p)
  }

  def matches(actualMethod: HttpMethod, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)

  def toPf(basePath: Path): Filter => Binding

  def describeFor(basePath: Path): String = (pathFn(basePath).toString :: pathParams.map(_.toString()).toList).mkString("/")
}
