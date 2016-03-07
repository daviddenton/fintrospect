package presentation._1

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import io.fintrospect.ModuleSpec

class SearchApp {
  val service = ModuleSpec(Root).toService
  val searchService = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", ModuleSpec(Root).withRoute().toService)
}


object Environment extends App {
  new SearchApp
  Thread.currentThread().join()
}

/**
 * showcase: empty api and 404
 */