package examples.clients

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import examples.clients.BrewdogApiContract.lookupBeers
import io.fintrospect.configuration.{Credentials, Host, Port}
import io.fintrospect.filters.DebuggingFilters
import io.fintrospect.filters.RequestFilters.{AddHost, BasicAuthorization}
import io.fintrospect.parameters.Query
import io.fintrospect.{ProxyModule, RouteSpec}

object BrewdogApiHttp {
  def apply(): Service[Request, Response] = {
    AddHost(Host("punkapi.com").toAuthority(Port(443)))
      .andThen(BasicAuthorization(Credentials("22244d6b88574064bbbfe284f1631eaf", "")))
      .andThen(DebuggingFilters.PrintRequestAndResponse)
      .andThen(Http.client.withTlsWithoutValidation.newService("punkapi.com:443"))
  }
}

object BrewdogApiContract {
  private val brewedBefore = Query.optional.string("brewed_before")
  private val alcoholContent = Query.optional.int("abv_gt")

  val lookupBeers = RouteSpec("lookup beers")
      .taking(brewedBefore)
      .taking(alcoholContent)
    .at(Get) / "api" / "v1" / "beers"
}

object ProxyExample extends App {

  val module = ProxyModule("brewdog", BrewdogApiHttp())
    .withRoute(lookupBeers)

  Http.serve(":9000", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))

  Thread.currentThread().join()
}
