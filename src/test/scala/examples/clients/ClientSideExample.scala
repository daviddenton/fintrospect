package examples.clients

import java.time.LocalDate

import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import io.fintrospect.clients.ClientRoute
import io.fintrospect.parameters.{Body, Header, Path}
import io.fintrospect.util.HttpRequestResponseUtil._
import io.fintrospect.util.{ArgoUtil, PlainTextResponseBuilder}
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

/**
 * EXPERIMENTAL!!! This API is likely to change significantly in future releases.
 *
 * Simple example of how to define client endpoints using the same techniques as the server routes.
 * Note that the client will automatically reject (with a 400) any unknown or missing parameters, as per the
 * specified route. The response is also decorated with the anonymised route, allowing for each collection of
 * metrics about timing and number of requests going to the downsteam systems.
 */
object ClientSideExample extends App {

  Http.serve(":10000", new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      println(contentFrom(request))
      println(request.getUri)
      println(headersFrom(request))
      Future.value(PlainTextResponseBuilder.Ok(""))
    }
  })

  val localEchoService = Http.newService("localhost:10000")

  val theDate = Path.localDate("date")
  val theUser = Header.required.string("user")
  val body = Body.json(Option("body"))

  val localClient = ClientRoute()
    .taking(theUser)
    .body(body)
    .at(GET) / "firstSection" / theDate bindTo localEchoService

  val theCall = localClient(body -> ArgoUtil.obj(), theDate -> LocalDate.of(2015, 1, 1), theUser -> System.getenv("USER"))

  val response = Await.result(theCall)

  println("Response headers: " + headersFrom(response))
  println("Response: " + statusAndContentFrom(response))
}
