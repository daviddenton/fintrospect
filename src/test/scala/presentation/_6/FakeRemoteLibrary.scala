package presentation._6

import com.twitter.finagle.httpx.filter.Cors
import com.twitter.finagle.httpx.filter.Cors.HttpFilter
import com.twitter.finagle.httpx.path.Root
import com.twitter.finagle.httpx.{Request, Response}
import com.twitter.finagle.{Httpx, Service}
import com.twitter.util.Future
import io.fintrospect.FintrospectModule
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.text.PlainTextResponseBuilder._
import io.fintrospect.renderers.simplejson.SimpleJson
import presentation.Books


class FakeRemoteLibrary(books: Books) {
  def search(titlePart: String) = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      val results = books.titles().filter(_.toLowerCase.contains(titlePart.toLowerCase))
      OK(results.mkString(","))
    }
  }

  val service = FintrospectModule(Root, SimpleJson())
    .withRoute(RemoteBooks.route bindTo search)
    .toService

  val searchService = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Httpx.serve(":10000", searchService)
}
