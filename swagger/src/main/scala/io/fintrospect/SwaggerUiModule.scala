package io.fintrospect

import com.twitter.finagle.Filter
import com.twitter.finagle.http.path.{Path, Root}
import com.twitter.finagle.http.{Request, Response}

object SwaggerUiModule {
  private def addBaseUrl(path: Path) = Filter.mk[Request, Response, Request, Response] {
    (request, svc) => {
      svc(request).map(response => {
        if (request.uri.equals("/")) {
          response.contentString = response.contentString.replace("http://petstore.swagger.io/v2/swagger.json", path.toString)
        }
        response
      })
    }
  }

  def apply(targetModule: ModuleSpec[Request, Response], docPath: Path = Root, moduleFilter: Filter[Request, Response, Request, Response] = Filter.identity): Module =
    StaticModule(docPath, "io/fintrospect", moduleFilter.andThen(addBaseUrl(targetModule.basePath)))
}
