package presentation._1

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import io.fintrospect.FintrospectModule
import io.fintrospect.renderers.simplejson.SimpleJson

class SearchApp {
  val service = FintrospectModule(Root, SimpleJson()).toService
  val searchService = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", searchService)
}


object Environment extends App {
  new SearchApp
  Thread.currentThread().join()
}

/**
 * showcase: empty api and 404
 */