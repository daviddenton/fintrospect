package io.github.daviddenton.fintrospect.swagger2dot0

import argo.jdom.JsonNodeFactories._
import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.ModuleRoute
import io.github.daviddenton.fintrospect.util.ArgoUtil._

object SwaggerJson extends (Seq[ModuleRoute[SwDescription]] => JsonRootNode) {
  def apply(moduleRoutes: Seq[ModuleRoute[SwDescription]]): JsonRootNode = {
    val paths = moduleRoutes
      .groupBy(_.toString)
      .map { case (path, routes) => path -> obj(routes.map(_.describe))}.toSeq

    obj(
      "swagger" -> string("2.0"),
      "info" -> obj("title" -> string("title"), "version" -> string("version")),
      "basePath" -> string("/"),
      "paths" -> obj(paths)
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

/*
  "models": {
    "UserBusinessPartnerDocsView": {
      "id": "UserBusinessPartnerDocsView",
      "description": "UserBusinessPartnerDocsView",
      "properties": {
        "userId": {
          "description": null,
          "enum": [],
          "required": true,
          "type": "string"
        },
        "businessPartners": {
          "description": null,
          "enum": [],
          "required": true,
          "type": "Array",
          "uniqueItems": true,
          "items": {"$ref": "BusinessPartnerDocsView"}
        }
      }
    }
  },
}
 */
