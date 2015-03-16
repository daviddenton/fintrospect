package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.Requirement._
import io.github.daviddenton.fintrospect.parameters.{Parameter, PathParameter, Requirement}

class ModuleRoute(val description: Description, val on: On, basePath: Path, pathParams: Seq[PathParameter[_]]) {
  val allParams: Iterable[(Requirement, Parameter[_, _])] = description.requestParams.allParams ++ pathParams.map(Mandatory -> _)

  val allResponses = description.responses

  override def toString: String = (on.completeRoutePath(basePath).toString :: pathParams.map(_.toString()).toList).mkString("/")
}
