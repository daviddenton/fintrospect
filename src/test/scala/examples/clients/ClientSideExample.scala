package examples.clients

import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await._
import io.fintrospect.clients.ClientRoute
import io.fintrospect.parameters.{Header, Path}
import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.handler.codec.http.HttpMethod._
import util.Echo

/**
 * Simple example of how to define client endpoints using the same techniques as the server routes.
 * Note that the client will automatically reject (with a 400) any unknown or missing parameters, as per the
 * specified route. The response is also decorated with the anonymised route, allowing for each collection of
 * metrics about timing and number of requests going to the downsteam systems.
 */
object ClientSideExample extends App {

  Http.serve(":10000", Service.mk(Echo("hello")))

  val localEchoService = Http.newService("localhost:10000")

  val theDate = Path.localDate("date")
  val theUser = Header.required.string("user")

  val localClient = ClientRoute().taking(theUser).at(GET) / "firstSection" / theDate bindTo localEchoService

  val theCall = localClient(theDate -> "2015-01-01", theUser -> System.getenv("USER"))

  val response = result(theCall)

  println("Response headers: " + headersFrom(response))
  println("Response: " + statusAndContentFrom(response))
}
