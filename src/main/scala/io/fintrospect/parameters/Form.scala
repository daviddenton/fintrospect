package io.fintrospect.parameters

class Form(fields: collection.Map[String, Set[String]]) {
  def get(name: String): Option[String] = fields.get(name).map(_.mkString(","))
}
