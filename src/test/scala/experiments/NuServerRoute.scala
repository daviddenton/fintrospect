package experiments

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}

abstract class NuServerRoute(method: Method, pathFn: Path => Path) {
  def toPf(basePath: Path): PartialFunction[(Method, Path), Service[Request, Response]]

  def matches(actualMethod: Method, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)
}
