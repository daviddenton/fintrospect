package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.PathParameter

class ModuleRoute(val description: Description, val on: On, rootPath: Path, pathParams: Seq[PathParameter[_]]) {
  val allParams = pathParams.flatten ++ description.params
  val allResponses = description.responses

  override def toString: String = (on.completeRoutePath(rootPath).toString :: pathParams.map(_.toString()).toList).mkString("/")
}
