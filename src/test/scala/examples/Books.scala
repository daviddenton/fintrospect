package examples

import java.lang.Integer.parseInt

import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.util.Try

case class Book(title: String, author: String, pages: Int) {
  def toJson: JsonRootNode = obj("title" -> string(title), "pages" -> number(pages), "author" -> obj("name" -> string(author)))
}

object Book {
  def unapply(input: JsonNode): Option[Book] = Try(
    Book(input.getStringValue("title"), input.getStringValue("author", "name"), parseInt(input.getNumberValue("pages")))).toOption
}

class Books {
  private var knownBooks = Map[String, Book](
    "hp1" -> Book("hairy porker", "j.k oinking", 799),
    "fs1" -> Book("fifty shades of spray", "e.l racoon", 300),
    "si1" -> Book("a song of 5000 years", "george r.r housemartin", 1040)
  )

  def add(isbn: String, book: Book) = knownBooks += (isbn -> book)

  def list(): Iterable[Book] = knownBooks.values.toSeq.sortBy(_.title)

  def lookup(isbn: String): Option[Book] = knownBooks.get(isbn)

  def search(maxPages: Int, titleSearch: String): Iterable[Book] = list().filter {
    case book => book.title.contains(titleSearch) && book.pages <= maxPages
  }
}