package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.{Parameter, Requirement}

class ModuleRoute2(val description: Description, val pw: PathWrapper, val basePath: Path) {
  val allParams: List[(Requirement, Parameter[_])] = {
    (description.params ++ pw.flatMap(identity)).map(p => p.requirement -> p)
  }

  override def toString: String = (pw.completePath(basePath).toString :: pw.map(_.toString()).toList).mkString("/")
}
