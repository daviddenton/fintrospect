package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.{Parameter, PathParameter, Requirement}

class ModuleRoute(val description: Description, val on: On, val basePath: Path, pathParams: Seq[PathParameter[_]]) {
  val allParams: List[(Requirement, Parameter[_])] = description.optional.map(Requirement.Optional -> _) ++
    (description.required ++ pathParams).map(Requirement.Mandatory -> _)

  override def toString: String = (on.completeRoutePath(basePath).toString :: pathParams.map(_.toString()).toList).mkString("/")
}
