package io.fintrospect.renderers

/**
  * View model for use with templating system.
  */
trait View {
  val template: String = getClass.getName.replace('.', '/')
}
