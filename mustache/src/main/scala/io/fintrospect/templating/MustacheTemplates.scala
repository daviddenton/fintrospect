package io.fintrospect.templating

import java.io.{ByteArrayOutputStream, File, OutputStreamWriter}
import java.nio.charset.StandardCharsets

import com.github.mustachejava.resolver.{DefaultResolver, FileSystemResolver}
import com.github.mustachejava.{DefaultMustacheFactory, Mustache}
import com.twitter.io.Buf
import com.twitter.mustache.ScalaObjectHandler

object MustacheTemplates extends Templates {

  private def render(view: View, mustache: Mustache): Buf = {
    val outputStream = new ByteArrayOutputStream(4096)
    val writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
    try {
      mustache.execute(writer, view)
    } finally {
      writer.close()
    }

    Buf.ByteArray.Owned(outputStream.toByteArray)
  }

  def CachingClasspath(baseClasspathPackage: String = "."): TemplateRenderer = new TemplateRenderer {

    private val factory = new DefaultMustacheFactory(new DefaultResolver(baseClasspathPackage)) {
      setObjectHandler(new ScalaObjectHandler)
    }

    def toBuf(view: View): Buf = render(view, factory.compile(view.template + ".mustache"))
  }

  def Caching(baseTemplateDir: String): TemplateRenderer = new TemplateRenderer {

    private val factory = new DefaultMustacheFactory(new FileSystemResolver(new File(baseTemplateDir))) {
      setObjectHandler(new ScalaObjectHandler)
    }

    def toBuf(view: View): Buf = render(view, factory.compile(view.template + ".mustache"))
  }

  def HotReload(baseTemplateDir: String = "."): TemplateRenderer = new TemplateRenderer {

    class WipeableMustacheFactory extends DefaultMustacheFactory(new FileSystemResolver(new File(baseTemplateDir))) {
      setObjectHandler(new ScalaObjectHandler)
    }

    def toBuf(view: View): Buf = render(view, new WipeableMustacheFactory().compile(view.template + ".mustache"))
  }
}
