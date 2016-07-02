package io.fintrospect.templating

import com.gilt.handlebars.scala.Handlebars
import com.gilt.handlebars.scala.binding.dynamic._
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.formats.AbstractResponseBuilder
import io.fintrospect.templating.HandlebarsTemplateLoaders.CachingClasspath

/**
  * Used to convert View objects to Handlebars View files. This template caching Filter can be added as a module filter
  * to be applied to all routes in that module.
  * @param responseBuilder The ResponseBuilder to use - this identifies the content type that will be used.
  */
class RenderHandlebarsView(responseBuilder: AbstractResponseBuilder[_], templateLoader: TemplateLoader[Handlebars[Any]] = CachingClasspath("/"))
  extends Filter[Request, Response, Request, View] {

  import responseBuilder.implicits.statusToResponseBuilderConfig

  override def apply(request: Request, service: Service[Request, View]) = service(request)
    .flatMap { view => Ok(templateLoader.forView(view)(view)) }
}
