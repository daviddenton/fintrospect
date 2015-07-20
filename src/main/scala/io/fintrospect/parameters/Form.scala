package io.fintrospect.parameters

case class Form(private val fields: collection.Map[String, Set[String]]) extends Iterable[(String, Set[String])] {
  def +(key: String, value: Set[String]) = Form(fields + (key -> value))

  def get(name: String): Option[Seq[String]] = fields.get(name).map(_.toSeq)

  override def iterator: Iterator[(String, Set[String])] = fields.iterator
}

object Form {
  def apply(bindings: Iterable[FormFieldBinding]*): Form = bindings.flatten.foldLeft(new Form(Map.empty))((f, b) => b(f))
}