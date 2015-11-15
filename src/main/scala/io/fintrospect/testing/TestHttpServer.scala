package io.fintrospect.testing

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Future
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{FintrospectModule, ServerRoutes}

/**
 * Simple, insecure HTTP server which can be used for tests
 */
class TestHttpServer(port: Int, serverRoutes: ServerRoutes) {

  private var server: ListeningServer = null

  private val service = FintrospectModule(Root, SimpleJson()).withRoutes(serverRoutes).toService

  def start(): Future[Unit] = {
    server = Http.serve(s":$port", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service))
    Future.Done
  }

  def stop() = Option(server).map(_.close()).getOrElse(Future.Done)
}
