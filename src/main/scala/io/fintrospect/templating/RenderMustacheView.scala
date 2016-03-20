package io.fintrospect.templating

import java.io.{ByteArrayOutputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

import com.github.mustachejava.{DefaultMustacheFactory, Mustache}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf
import com.twitter.mustache.ScalaObjectHandler
import com.twitter.util.Future
import io.fintrospect.formats.AbstractResponseBuilder

import scala.collection.JavaConverters._

/**
  * Used to convert View objects to Mustache View files. This template caching Filter can be added as a module filter
  * to be applied to all routes in that module.
  * @param responseBuilder The ResponseBuilder to use - this identifies the content type that will be used.
  * @param baseTemplateDir base template directory to load resources from.
  */
class RenderMustacheView(responseBuilder: AbstractResponseBuilder[_], baseTemplateDir: String = ".")
  extends Filter[Request, Response, Request, View] {

  import responseBuilder.statusToResponseBuilderConfig

  private val classToMustache = new ConcurrentHashMap[Class[_], Mustache]().asScala

  private val factory = new DefaultMustacheFactory(baseTemplateDir) {
    setObjectHandler(new ScalaObjectHandler)
  }

  private def loadMustache(view: View) = classToMustache.getOrElseUpdate(view.getClass, {
    factory.compile(view.template + ".mustache")
  })

  override def apply(request: Request, service: Service[Request, View]): Future[Response] = {
    service(request)
      .flatMap {
        view => {
          val outputStream = new ByteArrayOutputStream(4096)
          val writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
          try {
            loadMustache(view).execute(writer, view)
          } finally {
            writer.close()
          }

          Status.Ok(Buf.ByteArray.Owned(outputStream.toByteArray))
        }
      }
  }
}
