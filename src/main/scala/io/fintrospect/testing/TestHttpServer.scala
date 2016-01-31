package io.fintrospect.testing

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, ListeningServer, Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{ServerRoute, ModuleSpec, ServerRoutes}

/**
  * Simple, insecure HTTP server which can be used for tests
  */
class TestHttpServer(port: Int, serverRoutes: ServerRoutes[Response]) {

  def this(port: Int, route: ServerRoute[Response]) = this(port, new ServerRoutes[Response] { add(route)})

  private var overrideStatus: Option[Status] = Option.empty

  private val possibleError = new SimpleFilter[Request, Response] {
    override def apply(request: Request, service: Service[Request, Response]) = overrideStatus
      .map(s => Future.value(Response(s)))
      .getOrElse(service(request))
  }

  private var server: ListeningServer = null

  private val service = ModuleSpec(Root, SimpleJson()).withRoutes(serverRoutes).toService

  /**
    * Override the status code returned by the server
    */
  def respondWith(status: Status) = overrideStatus = if (status == Status.Ok) None else Option(status)

  def start(): Future[Unit] = {
    overrideStatus = Option.empty
    server = Http.serve(s":$port",
      new HttpFilter(Cors.UnsafePermissivePolicy)
        .andThen(possibleError)
        .andThen(service))
    Future.Done
  }

  def stop() = Option(server).map(_.close()).getOrElse(Future.Done)
}
