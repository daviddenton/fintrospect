package cookbook.finagle


object SSL_Client_Example extends App {

  import com.twitter.finagle.Http
  import com.twitter.finagle.http.Request
  import com.twitter.util.Await.result
  import io.fintrospect.filters.RequestFilters.AddUserAgent

  val client = Http.client.withTls("api.github.com").newService("api.github.com:443")

  val request = Request("/users/daviddenton/repos")

  println(result(AddUserAgent("Fintrospect client").andThen(client)(request)).contentString)
}