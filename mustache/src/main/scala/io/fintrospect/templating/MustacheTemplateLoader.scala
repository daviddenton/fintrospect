package io.fintrospect.templating

import java.io.File

import com.github.mustachejava.resolver.{DefaultResolver, FileSystemResolver}
import com.github.mustachejava.{DefaultMustacheFactory, Mustache}
import com.twitter.mustache.ScalaObjectHandler



object MustacheTemplateLoader {

  /**
    * Loads and caches templates from the compiled classpath
    * @param baseClasspathPackage the root package to load from (defaults to
    */
  def CachingClasspath(baseClasspathPackage: String = "."): TemplateLoader[Mustache] = new TemplateLoader[Mustache] {

    private val factory = new DefaultMustacheFactory(new DefaultResolver(baseClasspathPackage)) {
      setObjectHandler(new ScalaObjectHandler)
    }

    override def forView(view: View): Mustache = factory.compile(view.template + ".mustache")
  }

  /**
    * Load and caches templates from a file path
    * @param baseTemplateDir the root path to load templates from
    */
  def Caching(baseTemplateDir: String): TemplateLoader[Mustache] = new TemplateLoader[Mustache] {

    private val factory = new DefaultMustacheFactory(new FileSystemResolver(new File(baseTemplateDir))) {
      setObjectHandler(new ScalaObjectHandler)
    }

    override def forView(view: View): Mustache = factory.compile(view.template + ".mustache")
  }

  /**
    * Hot-reloads (no-caching) templates from a file path
    * @param baseTemplateDir the root path to load templates from
    */
  def HotReload(baseTemplateDir: String = "."): TemplateLoader[Mustache] = new TemplateLoader[Mustache] {

    class WipeableMustacheFactory extends DefaultMustacheFactory(new FileSystemResolver(new File(baseTemplateDir))) {
      setObjectHandler(new ScalaObjectHandler)
    }

    override def forView(view: View): Mustache = new WipeableMustacheFactory().compile(view.template + ".mustache")
  }
}
