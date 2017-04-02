package cookbook.finagle



object Http1dot1_Client_Example extends App {

  import com.twitter.finagle.Http
  import com.twitter.finagle.http.Request
  import com.twitter.util.Await.result
  import io.fintrospect.configuration.{Host, Port}
  import io.fintrospect.filters.RequestFilters.AddHost

  val authority = Host("pokeapi.co").toAuthority(Port._80)
  val client = AddHost(authority)
    .andThen(Http.newService(authority.toString))

  println(result(client(Request("/"))).contentString)
}

