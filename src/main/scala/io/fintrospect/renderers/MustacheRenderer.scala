package io.fintrospect.renderers

import java.io.{ByteArrayOutputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

import com.github.mustachejava.{DefaultMustacheFactory, Mustache}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Response, Status}
import com.twitter.io.Buf
import com.twitter.mustache.ScalaObjectHandler
import com.twitter.util.Future
import io.fintrospect.formats.ResponseBuilderMethods

import scala.collection.JavaConverters._

class MustacheRenderer(builder: ResponseBuilderMethods[_], baseTemplateDir: String = ".")
  extends Service[View, Response] {

  import builder._

  private val classToMustache = new ConcurrentHashMap[Class[_], Mustache]().asScala

  private val factory = new DefaultMustacheFactory(baseTemplateDir) {
    setObjectHandler(new ScalaObjectHandler)
  }

  private def loadMustache(view: View) = classToMustache.getOrElseUpdate(view.getClass, {
    factory.compile(view.template + ".mustache")
  })

  override def apply(view: View): Future[Response] = {
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
