package io.fintrospect.templating

trait TemplateLoader[T] {
  def forView(view: View): T
}
