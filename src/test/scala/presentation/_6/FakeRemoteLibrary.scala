package presentation._6

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.util.PlainTextResponseBuilder
import io.fintrospect.{CorsFilter, FintrospectModule}
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import presentation.Books


class FakeRemoteLibrary(books: Books) {
  def search(titlePart: String) = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      println("i go this", titlePart)
      val results = books.titles().filter(_.toLowerCase.contains(titlePart.toLowerCase))
      Future.value(PlainTextResponseBuilder.Ok(results.mkString(",")))
    }
  }

  val service = FintrospectModule(Root, SimpleJson())
    .withRoute(RemoteBooks.route bindTo search)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":10000", searchService)
}
