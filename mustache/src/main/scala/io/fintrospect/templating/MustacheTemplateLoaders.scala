package io.fintrospect.templating

import java.io.File

import com.github.mustachejava.resolver.{DefaultResolver, FileSystemResolver}
import com.github.mustachejava.{DefaultMustacheFactory, Mustache}
import com.twitter.mustache.ScalaObjectHandler



object MustacheTemplateLoaders extends TemplateLoaders[Mustache] {

  def CachingClasspath(baseClasspathPackage: String = "."): TemplateLoader[Mustache] = new TemplateLoader[Mustache] {

    private val factory = new DefaultMustacheFactory(new DefaultResolver(baseClasspathPackage)) {
      setObjectHandler(new ScalaObjectHandler)
    }

    override def forView(view: View): Mustache = factory.compile(view.template + ".mustache")
  }

  def Caching(baseTemplateDir: String): TemplateLoader[Mustache] = new TemplateLoader[Mustache] {

    private val factory = new DefaultMustacheFactory(new FileSystemResolver(new File(baseTemplateDir))) {
      setObjectHandler(new ScalaObjectHandler)
    }

    override def forView(view: View): Mustache = factory.compile(view.template + ".mustache")
  }

  def HotReload(baseTemplateDir: String = "."): TemplateLoader[Mustache] = new TemplateLoader[Mustache] {

    class WipeableMustacheFactory extends DefaultMustacheFactory(new FileSystemResolver(new File(baseTemplateDir))) {
      setObjectHandler(new ScalaObjectHandler)
    }

    override def forView(view: View): Mustache = new WipeableMustacheFactory().compile(view.template + ".mustache")
  }
}
