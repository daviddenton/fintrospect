package io.fintrospect

import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.json.Argo.JsonFormat.{array, obj, string}
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}

object SwaggerSystem {
  def render(apiInfo: ApiInfo, modules: ModuleSpec[Request, Response]*): Unit = {
    val a = obj(
      "basePath" -> string("basePath"),
      "swaggerVersion" -> string("2.0"),
      "apiVersion" -> string(apiInfo.version),
      "apis" -> array(modules.flatMap {
        m => m.moduleRenderer match {
          case Swagger2dot0Json(moduleApiInfo) => Seq(
            obj(
              "path" -> string(m.descriptionRoutePath(m.basePath).toString),
              "description" -> string(moduleApiInfo.title)
            )
          )
          case _ => Nil
        }
      })
    )
  }

}
