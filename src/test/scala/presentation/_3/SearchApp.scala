package presentation._3

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import io.fintrospect.parameters.Query
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.util.PlainTextResponseBuilder
import io.fintrospect.{CorsFilter, FintrospectModule, RouteSpec}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}
import presentation.Books

class SearchRoute(books: Books) {
  private val titlePartParam = Query.required.string("titlePart")

  def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      val titlePart = titlePartParam <-- request
      val results = books.titles().filter(_.toLowerCase.contains(titlePart.toLowerCase))
      Future.value(PlainTextResponseBuilder.Ok(results.toString()))
    }
  }

  val route = RouteSpec("search books")
    .taking(titlePartParam)
    .at(HttpMethod.GET) / "search" bindTo search
}


class SearchApp(books: Books) {
  val service = FintrospectModule(Root, SimpleJson())
    .withRoute(new SearchRoute(books).route)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", searchService)
}


object Environment extends App {
  new SearchApp(new Books)
  Thread.currentThread().join()
}

/**
 * showcase: missing query param and successful search
 */
