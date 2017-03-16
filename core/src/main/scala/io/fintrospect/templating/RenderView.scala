package io.fintrospect.templating

import com.google.common.net.HttpHeaders.LOCATION
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import io.fintrospect.formats.AbstractResponseBuilder
import io.fintrospect.templating.View.Redirect

/**
  * Used to render View objects to views based on template files. This template Filter can be added as a module filter
  * to be applied to all routes in that module. If passed a View.Redirect instance (as is common after a PUT, POST) a redirect is
  * generated instead.
  *
  * @param responseBuilder The ResponseBuilder to use - this identifies the content type that will be used.
  * @param renderer        The TemplateLoader to use - this handles the conversion of the View to text.
  */
class RenderView[T](responseBuilder: AbstractResponseBuilder[T], renderer: TemplateRenderer)
  extends Filter[Request, Response, Request, View] {

  override def apply(request: Request, service: Service[Request, View]): Future[Response] = service(request) map {
    case Redirect(location, status) => responseBuilder.HttpResponse(status).withHeaders(LOCATION -> location)
    case view => responseBuilder.HttpResponse(view.status).withContent(renderer.toBuf(view))
  }
}
