package presentation._1

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import io.fintrospect.RouteModule

class SearchApp {
  val service = RouteModule(Root).toService
  val searchService = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", RouteModule(Root).withRoute().toService)
}


object Environment extends App {
  new SearchApp
  Thread.currentThread().join()
}

/**
 * showcase: empty api and 404
 */