package io.fintrospect.templating

import java.io.File
import java.util.concurrent.ConcurrentHashMap

import com.gilt.handlebars.scala.Handlebars
import com.gilt.handlebars.scala.binding.dynamic._

import scala.collection.JavaConverters._
import scala.io.Source.fromInputStream

object HandlebarsTemplateLoaders extends TemplateLoaders[Handlebars[Any]] {

  def CachingClasspath(baseClasspathPackage: String = "."): TemplateLoader[Handlebars[Any]] = new TemplateLoader[Handlebars[Any]] {

    private val classToTemplate = new ConcurrentHashMap[Class[_], Handlebars[Any]]().asScala

    def loadView[T <: View](view: T): Handlebars[Any] =
      classToTemplate.getOrElseUpdate(view.getClass,
        Option(getClass.getResourceAsStream("/" + view.template + ".hbs"))
          .map(fromInputStream)
          .map(t => Handlebars[Any](t.mkString))
          .getOrElse({
            throw new ViewNotFound(view)
          })
      )

    override def forView(view: View): Handlebars[Any] = loadView(view)
  }

  def Caching(baseTemplateDir: String): TemplateLoader[Handlebars[Any]] = {
    val baseDir = new File(baseTemplateDir)

    new TemplateLoader[Handlebars[Any]] {
      // add caching...
      override def forView(view: View): Handlebars[Any] = Handlebars(new File(baseDir, view.template + ".hbs"))
    }
  }

  def HotReload(baseTemplateDir: String = "."): TemplateLoader[Handlebars[Any]] = new TemplateLoader[Handlebars[Any]] {
    override def forView(view: View): Handlebars[Any] = Handlebars[Any](new File(baseTemplateDir, view.template + ".hbs"))
  }
}
