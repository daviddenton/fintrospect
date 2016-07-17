package io.fintrospect.testing

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Future
import io.fintrospect.{ServerRoute, ServerRoutes}


/**
  * Simple, insecure HTTP server which can be used for tests
  */
class TestHttpServer(port: Int, serverRoutes: ServerRoutes[Request, Response]) extends OverridableHttpService(serverRoutes){

  private var server: ListeningServer = _

  def this(port: Int, route: ServerRoute[Request, Response]) = this(port, new ServerRoutes[Request, Response] {
    add(route)
  })

  def start(): Future[Unit] = {
    respondWith(Status.Ok)

    server = Http.serve(s":$port",
      new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service))
    Future.Done
  }

  def stop() = Option(server).map(_.close()).getOrElse(Future.Done)
}
