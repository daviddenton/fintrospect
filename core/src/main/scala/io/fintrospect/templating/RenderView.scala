package io.fintrospect.templating

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.formats.AbstractResponseBuilder

/**
  * Used to render View objects to views based on template files. This template Filter can be added as a module filter
  * to be applied to all routes in that module.
  * @param responseBuilder The ResponseBuilder to use - this identifies the content type that will be used.
  * @param renderer The TemplateLoader to use - this handles the conversion of the View to text.
  */
class RenderView(responseBuilder: AbstractResponseBuilder[_], renderer: TemplateRenderer)
  extends Filter[Request, Response, Request, View] {

  import responseBuilder.implicits.statusToResponseBuilderConfig

  override def apply(request: Request, service: Service[Request, View]) = service(request)
    .flatMap { view => Ok(renderer.toBuf(view)) }
}
