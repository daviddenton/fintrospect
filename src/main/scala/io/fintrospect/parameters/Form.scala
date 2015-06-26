package io.fintrospect.parameters

class Form(fields: collection.Map[String, Set[String]]) extends Iterable[(String, Set[String])] {

  def get(name: String): Option[String] = fields.get(name).map(_.mkString(","))

  override def iterator: Iterator[(String, Set[String])] = fields.iterator
}

object Form {
  def apply(bindings: Iterable[FormFieldBinding]*): Form = new Form(bindings.flatten.map(b => (b.key, Set(b.value))).toMap)
}