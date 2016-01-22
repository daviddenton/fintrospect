package io.fintrospect.renderers

trait View {
  val template: String = getClass.getName.replace('.', '/')
}
