package io.fintrospect.templating

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters._
import scala.io.Source.fromInputStream

class CachingClasspathViews[X](fn: String => X, suffix: String) {
  private val classToTemplate = new ConcurrentHashMap[Class[_], X]().asScala

  def loadView[T <: View](view: T): X = classToTemplate.getOrElseUpdate(view.getClass, {
    Option(getClass.getResourceAsStream("/" + view.template + suffix))
      .map(fromInputStream)
      .map(t => fn(t.mkString))
      .getOrElse({throw new ViewNotFound(view)})
  })
}
