package examples.clients

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.fintrospect.configuration.{Credentials, Host, Port}
import io.fintrospect.filters.RequestFilters.{AddHost, BasicAuthorization}
import io.fintrospect.parameters.Query
import io.fintrospect.{Contract, ContractEndpoint, ContractProxyModule, RouteSpec}

import scala.language.reflectiveCalls

/**
  * This example shows how to use a contract to provide a Swagger-documented Proxy API for a set of remote routes.
  */
object ContractProxyExample extends App {

  val proxyModule = ContractProxyModule("brewdog", BrewdogApiHttp(), BrewdogApiContract)

  Await.ready(
    Http.serve(":9000", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(proxyModule.toService))
  )
}

object BrewdogApiHttp {
  private val apiAuthority = Host("punkapi.com").toAuthority(Port(443))

  def apply(): Service[Request, Response] = {
    AddHost(apiAuthority)
      .andThen(BasicAuthorization(Credentials("22244d6b88574064bbbfe284f1631eaf", "")))
      .andThen(Http.client.withTlsWithoutValidation.newService(apiAuthority.toString))
  }
}

object BrewdogApiContract extends Contract {

  object LookupBeers extends ContractEndpoint {
    val brewedBefore = Query.optional.string("brewed_before", "e.g. 01-2010 (format is mm-yyyy)")
    val alcoholContent = Query.optional.int("abv_gt", "Minimum alcohol %")

    override val route =
      RouteSpec("lookup beers")
        .taking(brewedBefore)
        .taking(alcoholContent)
        .at(Get) / "api" / "v1" / "beers"
  }

  object RandomBeer extends ContractEndpoint {
    override val route = RouteSpec("get a random beer recipe")
      .at(Get) / "api" / "v1" / "beers" / "random"
  }

}
