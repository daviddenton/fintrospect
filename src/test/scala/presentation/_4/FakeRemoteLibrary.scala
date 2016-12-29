package presentation._4

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import io.fintrospect.RouteModule
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.renderers.simplejson.SimpleJson
import presentation.Books


class FakeRemoteLibrary(books: Books) {
  def search(titlePart: String) = Service.mk[Request, Response] {
    _ => Ok(books.titles().filter(_.toLowerCase.contains(titlePart.toLowerCase)).mkString(","))
  }

  val service = RouteModule(Root, SimpleJson())
    .withRoute(RemoteBooks.route bindTo search)
    .toService

  val searchService = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":10000", searchService)
}
