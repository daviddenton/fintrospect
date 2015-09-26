package presentation._5

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import io.fintrospect._
import io.fintrospect.parameters.Query
import io.fintrospect.renderers.swagger2dot0.Swagger2dot0Json
import io.fintrospect.util.json.Argo
import io.fintrospect.util.json.Argo.JsonFormat._
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse, HttpResponseStatus}
import presentation.Book


class SearchRoute(books: RemoteBooks) {
  private val titlePartParam = Query.required.string("titlePart")

  def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      val titlePart = titlePartParam <-- request

      books.search(titlePart)
        .map(results => results.split(",").map(Book(_)).toSeq)
        .map(books => Argo.ResponseBuilder.Ok(array(books.map(_.toJson))))
    }
  }

  val route = RouteSpec("search books")
    .taking(titlePartParam)
    .returning(ResponseSpec.json(HttpResponseStatus.OK -> "search results", array(Book("1984").toJson)))
    .at(HttpMethod.GET) / "search" bindTo search
}


class SearchApp {
  private val apiInfo = ApiInfo("search some books", "1.0", Option("an api for searching our book collection"))

  val service = FintrospectModule(Root, Swagger2dot0Json(apiInfo))
    .withRoute(new SearchRoute(new RemoteBooks).route)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", searchService)
}

