package io.fintrospect.testing

import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{RouteModule, ServerRoutes}

/**
  * Provides the ability to wrap a pre-constructed service and then override the
  * HTTP response code that will be returned. Useful in testing scenarios.
  */
class OverridableHttpService[T](rawSvc: Service[Request, Response]) {

  def this(RouteModule: RouteModule[Request, Response]) = this(RouteModule.toService)

  def this(serverRoutes: ServerRoutes[Request, Response]) = this(
    RouteModule(Root,
      SimpleJson())
      .withRoutes(serverRoutes))

  private var overrideStatus = Option.empty[Status]

  private val possibleError = new SimpleFilter[Request, Response] {
    override def apply(request: Request, service: Service[Request, Response]) = overrideStatus
      .map(s => Future.value(Response(s)))
      .getOrElse(service(request))
  }

  /**
    * Override the status code returned by the server
    */
  def respondWith(status: Status) = overrideStatus = if (status == Status.Ok) None else Option(status)

  val service = possibleError.andThen(rawSvc)
}
