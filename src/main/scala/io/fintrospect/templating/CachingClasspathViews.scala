package io.fintrospect.templating

import java.util.concurrent.ConcurrentHashMap

import com.twitter.io.Buf
import com.twitter.util.Future
import io.fintrospect.formats.AbstractResponseBuilder
import io.fintrospect.templating.View

import scala.collection.JavaConverters._
import scala.io.Source._

class CachingClasspathViews[X](fn: String => X, suffix: String) {
  private val classToTemplate = new ConcurrentHashMap[Class[_], X]().asScala

  def loadView[T <: View](view: T): X = classToTemplate.getOrElseUpdate(view.getClass, {
    val s = "/" + view.template + suffix
    Option(getClass.getResourceAsStream(s))
      .map(fromInputStream)
      .map(t => fn(t.mkString))
      .getOrElse({throw new ViewNotFound(view)})
  })
}
