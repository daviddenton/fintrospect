package io.fintrospect

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Path
import io.fintrospect.parameters.{Binding, PathBinding, PathParameter}

class ReverseRouter(root: Path, route: UnboundRoute, pathParams: PathParameter[_]*) {
  private val providedBindings = pathParams.filter(_.isFixed).map(p => new PathBinding(p, p.name))

  def apply(bindings: Iterable[Binding]*): Path = route.pathFn(root) / buildRequest(bindings.flatten ++ providedBindings).uri.dropWhile(_ == '/')

  private def buildRequest(suppliedBindings: Seq[Binding]): Request = suppliedBindings
    .sortBy(p => pathParams.indexOf(p.parameter))
    .foldLeft(RequestBuilder(route.method)) { (requestBuild, next) => next(requestBuild) }.build()
}
