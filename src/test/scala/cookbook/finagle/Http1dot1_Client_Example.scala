package cookbook.finagle

import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.util.Await.result
import io.fintrospect.configuration.{Host, Port}
import io.fintrospect.filters.RequestFilters

object Http1dot1_Client_Example extends App {

  val authority = Host("pokeapi.co").toAuthority(Port._80)
  val client = RequestFilters.AddHost(authority)
    .andThen(Http.newService(authority.toString))

  println(result(client(Request("/"))).contentString)
}

