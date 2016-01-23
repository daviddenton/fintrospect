package io.fintrospect.templating

import java.util.concurrent.ConcurrentHashMap

import com.gilt.handlebars.scala.Handlebars
import com.gilt.handlebars.scala.binding.dynamic._
import com.twitter.io.Buf
import com.twitter.util.Future
import io.fintrospect.formats.AbstractResponseBuilder
import io.fintrospect.templating.View

import scala.collection.JavaConverters._
import scala.io.Source._

class CachingClasspathHandlebarsViews {
  private val classToTemplate = new ConcurrentHashMap[Class[_], Handlebars[Any]]().asScala

  def loadView[T <: View](view: T): Handlebars[Any] = classToTemplate.getOrElseUpdate(view.getClass, {
    val s = "/" + view.template + ".hbs"
    Option(getClass.getResourceAsStream(s))
      .map(fromInputStream)
      .map(t => Handlebars(t.mkString))
      .getOrElse({throw new ViewNotFound(view)})
  })
}
