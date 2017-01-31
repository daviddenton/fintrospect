package cookbook.core


object Extracting_Upstream_Responses extends App {

  import argo.jdom.JsonNode
  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.result
  import io.fintrospect.configuration.{Authority, Host, Port}
  import io.fintrospect.filters.RequestFilters.AddHost
  import io.fintrospect.parameters.{Body, BodySpec, Path}
  import io.fintrospect.util.{Extracted, ExtractionFailed}
  import io.fintrospect.{RouteClient, RouteSpec}

  case class Pokemon(id: Int, name: String, weight: Int)

  object Pokemon {
    def from(j: JsonNode) = Pokemon(j.getNumberValue("id").toInt,
      j.getStringValue("name"),
      j.getNumberValue("weight").toInt)
  }

  val authority: Authority = Host("pokeapi.co").toAuthority(Port._80)

  val http: Service[Request, Response] = AddHost(authority).andThen(Http.newService(authority.toString))

  val id = Path.int("pokemonId")

  val client: RouteClient = RouteSpec().at(Get) / "api" / "v2" / "pokemon" / id / "" bindToClient http

  val spec: BodySpec[Pokemon] = BodySpec.json().map(Pokemon.from)

  val nameAndWeight: Body[Pokemon] = Body(spec)

  def reportOnPokemon(pid: Int) =
    println(
      result(client(id --> pid).map(nameAndWeight.<--?)) match {
        case Extracted(Some(pokemon)) => s"i found a pokemon: $pokemon"
        case Extracted(None) => s"there is no pokemon with id $pid"
        case ExtractionFailed(errors) => s"problem extracting response $errors"
      }
    )

  reportOnPokemon(1)
  reportOnPokemon(11)
  reportOnPokemon(9999)
}
