package presentation

import argo.jdom.JsonNode
import io.fintrospect.formats.Argo.JsonFormat.{number, obj, string}

case class Book(title: String) {
  def toJson = obj(
    "title" -> string(title),
    "titleLengthInWords" -> number(title.split(" ").length)
  )
}

object Book {
  def fromJson(jsonNode: JsonNode) = Book(jsonNode.getStringValue("title"))
}