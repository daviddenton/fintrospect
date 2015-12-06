package presentation._6

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
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
      Ok(results.mkString(","))
    }
  }

  val service = FintrospectModule(Root, SimpleJson())
    .withRoute(RemoteBooks.route bindTo search)
    .toService

  val searchService = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":10000", searchService)
}
