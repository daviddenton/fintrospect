package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.PathParameter

class ModuleRoute(val description: Description, rootPath: Path, pathParams: Seq[PathParameter[_]]) {
  val params = pathParams.flatten ++ description.params

  override def toString: String = (description.complete(rootPath).toString :: pathParams.map(_.toString).toList).mkString("/")
}
