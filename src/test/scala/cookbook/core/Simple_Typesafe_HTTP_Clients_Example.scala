package cookbook.core


// fintrospect-core
object Simple_Typesafe_HTTP_Clients_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await
  import io.fintrospect.configuration.{Authority, Host, Port}
  import io.fintrospect.filters.RequestFilters.AddHost
  import io.fintrospect.parameters.Path
  import io.fintrospect.{RouteClient, RouteSpec}

  val authority: Authority = Host("pokeapi.co").toAuthority(Port._80)

  val http: Service[Request, Response] = AddHost(authority).andThen(Http.newService(authority.toString))

  val id = Path.int("pokemonId")
  val client: RouteClient[Response] = RouteSpec().at(Get) / "api" / "v2" / "pokemon" / id / "" bindToClient http

  println(Await.result(client(id --> 40)).contentString)
}

//http://pokeapi.co/api/v2/pokemon/11/