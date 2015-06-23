package io.fintrospect.parameters

class Form(fields: collection.Map[String, Set[String]]) extends Iterable[(String, Set[String])]{

  def +(pair: (String, String)): Form = ???

  def get(name: String): Option[String] = fields.get(name).map(_.mkString(","))

  override def iterator: Iterator[(String, Set[String])] = fields.iterator
}

