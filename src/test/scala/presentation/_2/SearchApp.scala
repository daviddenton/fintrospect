package presentation._2

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import io.fintrospect.renderers.swagger2dot0.Swagger2dot0Json
import io.fintrospect.util.PlainTextResponseBuilder
import io.fintrospect.{ApiInfo, CorsFilter, FintrospectModule, RouteSpec}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}
import presentation.Books

class SearchApp(books: Books) {
  def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      val results = books.titles()
      Future.value(PlainTextResponseBuilder.Ok(results.toString()))
    }
  }

  private val apiInfo = ApiInfo("search some books", "1.0", Option("an api for searching our book collection"))

  val service = FintrospectModule(Root, Swagger2dot0Json(apiInfo))
    .withRoute(RouteSpec("search books").at(HttpMethod.GET) / "search" bindTo search)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", searchService)
}


object Environment extends App {
  new SearchApp(new Books)
  Thread.currentThread().join()
}

/**
 * showcase: api and retrieve list of titles
 */
