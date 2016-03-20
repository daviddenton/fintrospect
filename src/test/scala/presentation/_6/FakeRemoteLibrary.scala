package presentation._6

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import io.fintrospect.ModuleSpec
import io.fintrospect.formats.PlainText.ResponseBuilder.toResponseBuilder
import io.fintrospect.formats.ResponseBuilder.toFuture
import io.fintrospect.renderers.simplejson.SimpleJson
import presentation.Books


class FakeRemoteLibrary(books: Books) {
  def search(titlePart: String) =  Service.mk[Request, Response] {
    request => {
      val results = books.titles().filter(_.toLowerCase.contains(titlePart.toLowerCase))
      Ok(results.mkString(","))
    }
  }

  val service = ModuleSpec(Root, SimpleJson())
    .withRoute(RemoteBooks.route bindTo search)
    .toService

  val searchService = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":10000", searchService)
}
