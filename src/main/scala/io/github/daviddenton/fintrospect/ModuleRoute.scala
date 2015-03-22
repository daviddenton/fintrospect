package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.PathParameter
import io.github.daviddenton.fintrospect.parameters.Requirement._

class ModuleRoute(val description: Description, val on: On, val basePath: Path, pathParams: Seq[PathParameter[_]]) {
  val allParams = description.allParams ++ pathParams.map(Mandatory -> _)

  val allResponses = description.responses

  override def toString: String = (on.completeRoutePath(basePath).toString :: pathParams.map(_.toString()).toList).mkString("/")
}
