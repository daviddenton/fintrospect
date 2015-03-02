package io.github.daviddenton.fintrospect.swagger.v1dot1

import argo.jdom.JsonNodeFactories._
import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.ModuleRoute
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.collection.JavaConversions._

object SwaggerV1dot1Json extends (Seq[ModuleRoute[SwDescription]] => JsonRootNode) {
  def apply(moduleRoutes: Seq[ModuleRoute[SwDescription]]): JsonRootNode = {
    val api = moduleRoutes
      .groupBy(_.toString)
      .map { case (path, routes) => obj("path" -> string(path), "operations" -> array(routes.map(_.describe._2): _*))}

    obj(
      "swaggerVersion" -> string("1.1"),
      "resourcePath" -> string("/"),
      "apis" -> array(asJavaIterable(api))
      //    "definitions" -> obj(
      //      "User" -> obj(
      //        "properties" -> obj(
      //          "id" -> obj(
      //            "type" -> "integer",
      //            "format" -> "int64"
      //          )
      //        )
      //      )
      //    )
    )
  }
}
