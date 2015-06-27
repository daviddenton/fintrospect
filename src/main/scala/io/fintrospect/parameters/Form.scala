package io.fintrospect.parameters

class Form (fields: collection.Map[String, Set[String]]) extends Iterable[(String, Set[String])] {
  def +(key: String, value: Set[String]) = new Form(fields + (key -> value))

  def get(name: String): Option[String] = fields.get(name).map(_.mkString(","))

  override def iterator: Iterator[(String, Set[String])] = fields.iterator
}

object Form {
  def apply(bindings: Iterable[FormFieldBinding]*): Form = bindings.flatten.foldLeft(Form())((f, b) => b(f))
}