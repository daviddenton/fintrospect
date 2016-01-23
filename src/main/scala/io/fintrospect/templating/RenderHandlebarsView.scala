package io.fintrospect.templating

import com.gilt.handlebars.scala.Handlebars
import com.gilt.handlebars.scala.binding.dynamic._
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import io.fintrospect.formats.AbstractResponseBuilder

/**
  * Used to convert View objects to Handlebars View files. This template caching Filter can be added as a module filter
  * to be applied to all routes in that module.
  * @param responseBuilder The ResponseBuilder to use - this identifies the content type that will be used.
  */
class RenderHandlebarsView(responseBuilder: AbstractResponseBuilder[_])
  extends Filter[Request, Response, Request, View] {

  import responseBuilder._

  private val loader = new CachingClasspathViews[Handlebars[Any]](s => Handlebars(s), ".hbs")

  override def apply(request: Request, service: Service[Request, View]): Future[Response] = service(request)
    .flatMap {
      view => {
        Status.Ok(loader.loadView(view)(view))}
    }
}
