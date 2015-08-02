package presentation._4

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import io.fintrospect.parameters.Query
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.util.PlainTextResponseBuilder
import io.fintrospect.{CorsFilter, FintrospectModule, RouteSpec}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

class SearchRoute(books: RemoteBooks) {
  private val titlePartParam = Query.required.string("titlePart")

  def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      val titlePart = titlePartParam <-- request

      books.search(titlePart)
        .map(results => PlainTextResponseBuilder.Ok(results))
    }
  }

  val route = RouteSpec("search books")
    .taking(titlePartParam)
    .at(HttpMethod.GET) / "search" bindTo search
}


class SearchApp {
  val service = FintrospectModule(Root, SimpleJson())
    .withRoute(new SearchRoute(new RemoteBooks).route)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", searchService)
}

