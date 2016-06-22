package io.fintrospect.templating

case class AtRoot(items: Seq[Item]) extends View {
  override val template = "AtRootBob"
}
