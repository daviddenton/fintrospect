package io.github.daviddenton.fintrospect.util

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Route
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.language.implicitConversions

class ArgoJsonResponseBuilder(descBuilder: (Path, Seq[Route]) => JsonRootNode) extends TypedResponseBuilder[JsonRootNode](
  () => new JsonResponseBuilder,
  descBuilder,
  badParameters => {
    val messages = badParameters.map(p => obj(
      "name" -> string(p.name),
      "type" -> string(p.where),
      "datatype" -> string(p.paramType.name),
      "required" -> boolean(p.requirement.required)
    ))

    obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages))
  })
