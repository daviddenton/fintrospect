package io.fintrospect.templating

import java.io.File
import java.util.concurrent.ConcurrentHashMap

import com.gilt.handlebars.scala.Handlebars
import com.gilt.handlebars.scala.binding.dynamic._
import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray.Owned

import scala.collection.JavaConverters._
import scala.io.Source

object HandlebarsTemplates extends Templates {

  def CachingClasspath(baseClasspathPackage: String = "."): TemplateRenderer =
    new TemplateRenderer {

      private val classToTemplate = new ConcurrentHashMap[Class[_], Handlebars[Any]]().asScala

      private def loadView[T <: View](view: T): Handlebars[Any] =
        classToTemplate.getOrElseUpdate(view.getClass,
          Option(getClass.getResourceAsStream("/" + view.template + ".hbs"))
            .map(t => Handlebars[Any](Source.fromInputStream(t).mkString))
            .getOrElse(throw new ViewNotFound(view))
        )

      override def toBuf(view: View): Buf = Owned(loadView(view)(view).getBytes)
    }

  def Caching(baseTemplateDir: String): TemplateRenderer = {
    val baseDir = new File(baseTemplateDir)

    new TemplateRenderer {
      private val classToTemplate = new ConcurrentHashMap[Class[_], Handlebars[Any]]().asScala

      private def loadView[T <: View](view: T): Handlebars[Any] =
        classToTemplate.getOrElseUpdate(view.getClass, {
          val file = new File(baseDir, view.template + ".hbs")
          if (!file.exists()) throw new ViewNotFound(view)
          Handlebars[Any](Source.fromFile(file).mkString)
        })

      override def toBuf(view: View): Buf = Owned(loadView(view)(view).getBytes)
    }
  }

  def HotReload(baseTemplateDir: String = "."): TemplateRenderer = new TemplateRenderer {
    override def toBuf(view: View): Buf = {
      val handlebars = Handlebars[Any](new File(baseTemplateDir, view.template + ".hbs"))
      Owned(handlebars(view).getBytes)
    }
  }
}
