package examples.clients

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import io.fintrospect.configuration.{Credentials, Host, Port}
import io.fintrospect.filters.RequestFilters.{AddHost, BasicAuthorization}
import io.fintrospect.parameters.Query
import io.fintrospect.{ProxyModule, RouteSpec}

object BrewdogApiHttp {
  private val apiAuthority = Host("punkapi.com").toAuthority(Port(443))

  def apply(): Service[Request, Response] = {
    AddHost(apiAuthority)
      .andThen(BasicAuthorization(Credentials("22244d6b88574064bbbfe284f1631eaf", "")))
      .andThen(Http.client.withTlsWithoutValidation.newService(apiAuthority.toString))
  }
}

object BrewdogApiContract {
  private val brewedBefore = Query.optional.string("brewed_before", "e.g. 01-2010 (format is mm-yyyy)")
  private val alcoholContent = Query.optional.int("abv_gt")

  val lookupBeers = RouteSpec("lookup beers")
      .taking(brewedBefore)
      .taking(alcoholContent)
    .at(Get) / "api" / "v1" / "beers"
}

/**
  * This example shows how to generate Proxy API documentation for a set of remote routes.
  */
object ProxyExample extends App {

  val proxyModule = ProxyModule("brewdog", BrewdogApiHttp())
    .withRoute(BrewdogApiContract.lookupBeers)

  Http.serve(":9000", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(proxyModule.toService))

  Thread.currentThread().join()
}
