package io.fintrospect.parameters

class NewForm(fields: collection.Map[String, Set[String]]) {
  def get(name: String): Option[String] = fields.get(name).map(_.mkString(","))
}
