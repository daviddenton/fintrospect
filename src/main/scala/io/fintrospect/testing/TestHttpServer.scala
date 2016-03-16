package io.fintrospect.testing

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Future
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{ModuleSpec, ServerRoute, ServerRoutes}


/**
  * Simple, insecure HTTP server which can be used for tests
  */
class TestHttpServer(port: Int, serverRoutes: ServerRoutes[Request, Response]) {

  private val inMemoryHttp = new OverridableHttpService(serverRoutes)

  private var server: ListeningServer = null

  def this(port: Int, route: ServerRoute[Request, Response]) = this(port, new ServerRoutes[Request, Response] {
    add(route)
  })

  /**
    * Override the status code returned by the server
    */
  def respondWith(status: Status) = inMemoryHttp.respondWith(status)

  def start(): Future[Unit] = {
    inMemoryHttp.respondWith(Status.Ok)

    server = Http.serve(s":$port",
      new HttpFilter(Cors.UnsafePermissivePolicy)
        .andThen(inMemoryHttp.service))
    Future.Done
  }

  def stop() = Option(server).map(_.close()).getOrElse(Future.Done)
}
