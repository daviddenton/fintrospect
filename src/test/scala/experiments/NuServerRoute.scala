package experiments

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}
import io.fintrospect.parameters.PathParameter

abstract class NuServerRoute(method: Method, pathFn: Path => Path,
                             pathParams: PathParameter[_]*) {
  def toPf(basePath: Path): PartialFunction[(Method, Path), Service[Request, Response]]

  def matches(actualMethod: Method, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)

  def describeFor(basePath: Path): String = (pathFn(basePath).toString +: pathParams.map(_.toString())).mkString("/")
}
