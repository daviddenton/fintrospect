package io.fintrospect.util

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.UnsupportedMediaType
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.fintrospect.ContentType
import io.fintrospect.formats.json.Argo.ResponseBuilder.implicits._
import io.fintrospect.parameters.Body
import io.fintrospect.renderers.ModuleRenderer
import io.fintrospect.renderers.simplejson.SimpleJson

/**
  * Service which allow multiple body types to be supported on a single route, based on the request Content-Type header
  */
object MultiBodyType {
  type SupportedContentType = (Body[_], Service[Request, Response])

  def apply(services: SupportedContentType*)(implicit moduleRenderer: ModuleRenderer = SimpleJson()): Service[Request, Response] = {
    val supportedContentTypes = Map(services.map(bs => ContentType(bs._1.contentType.value.toLowerCase) -> bs): _*)

    def validateAndRespond(request: Request, b: SupportedContentType) = {
      val invalidParams = b._1.validate(request).collect { case Left(l) => l }
      if (invalidParams.isEmpty) b._2(request) else Future.value(moduleRenderer.badRequest(invalidParams))
    }

    def handle(request: Request, contentType: ContentType): Future[Response] =
      supportedContentTypes.get(contentType)
        .map(pair => validateAndRespond(request, pair))
        .getOrElse(UnsupportedMediaType(contentType.value))

    Service.mk {
      request: Request => {
        (ContentType.header <-- request)
          .map(value => ContentType(value.toLowerCase()))
          .map(contentType => handle(request, contentType))
          .getOrElse(UnsupportedMediaType("missing Content-Type header"))
      }
    }
  }

}
