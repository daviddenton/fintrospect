package presentation._1

import com.twitter.finagle.Httpx
import com.twitter.finagle.httpx.filter.Cors
import com.twitter.finagle.httpx.path.Root
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{CorsFilter, FintrospectModule}

class SearchApp {
  val service = FintrospectModule(Root, SimpleJson()).toService
  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Httpx.serve(":9000", searchService)
}


object Environment extends App {
  new SearchApp
  Thread.currentThread().join()
}

/**
 * showcase: empty api and 404
 */