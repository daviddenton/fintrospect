package io.fintrospect.templating

import java.io.{ByteArrayOutputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets

import com.github.mustachejava.Mustache
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf
import com.twitter.util.Future
import io.fintrospect.formats.AbstractResponseBuilder
import io.fintrospect.templating.MustacheTemplateLoaders.CachingClasspath

/**
  * Used to convert View objects to Mustache View files. This template caching Filter can be added as a module filter
  * to be applied to all routes in that module.
  * @param responseBuilder The ResponseBuilder to use - this identifies the content type that will be used.
  * @param templateLoader template loader to use. Defaults to a Caching Classpath loader reading from the root of the classpath
  */
class RenderMustacheView(responseBuilder: AbstractResponseBuilder[_], templateLoader: TemplateLoader[Mustache] = CachingClasspath("/"))
  extends Filter[Request, Response, Request, View] {

  import responseBuilder.implicits.statusToResponseBuilderConfig

  override def apply(request: Request, service: Service[Request, View]): Future[Response] =
    service(request)
      .flatMap {
        view => {
          val outputStream = new ByteArrayOutputStream(4096)
          val writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
          try {
            templateLoader.forView(view).execute(writer, view)
          } finally {
            writer.close()
          }

          Ok(Buf.ByteArray.Owned(outputStream.toByteArray))
        }
      }
}
