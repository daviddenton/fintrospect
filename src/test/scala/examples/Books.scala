package examples

case class Book(title: String, author: String, pages: Int)

class Books {
  private val knownBooks = Map[String, Book](
    "hp1" -> Book("hairy porker", "j.k oinking", 799),
    "fs1" -> Book("fifty shades of spray", "e.l racoon", 300),
    "si1" -> Book("a song of 5000 years", "george r.r housemartin", 1040)
  )

  def list(): Iterable[Book] = knownBooks.values.toSeq.sortBy(_.title)

  def lookup(isbn: String): Option[Book] = knownBooks.get(isbn)

  def search(maxPages: Int, titleSearch: String): Iterable[Book] = list().filter {
    case book => book.title.contains(titleSearch) && book.pages <= maxPages
  }
}