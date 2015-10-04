package examples.clients

import java.time.LocalDate

import com.twitter.finagle.httpx.{Method, Request, Response}
import com.twitter.finagle.{Httpx, Service}
import com.twitter.util.{Await, Future}
import io.fintrospect.RouteSpec
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.text.PlainTextResponseBuilder._
import io.fintrospect.parameters._
import io.fintrospect.util.HttpRequestResponseUtil._

/**
 * Simple example of how to define client endpoints using the same techniques as the server routes.
 * Note that the client will automatically reject (with a 400) any unknown or missing parameters, as per the
 * specified route. The response is also decorated with the anonymised route, allowing for collection of
 * metrics about timing and number of requests going to the downsteam systems.
 */
object ClientSideExample extends App {

  Httpx.serve(":10000", new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      println("URL was " + request.uri)
      println("Headers were " + headersFrom(request))
      println("Content was " + contentFrom(request))
      Ok("")
    }
  })

  val httpClient = Httpx.newService("localhost:10000")

  val theDate = Path.localDate("date")
  val theWeather = Query.optional.string("weather")
  val theUser = Header.required.string("user")
  val gender = FormField.required.string("gender")
  val body = Body.form(gender)

  val client = RouteSpec()
    .taking(theUser)
    .taking(theWeather)
    .body(body)
    .at(Method.Get) / "firstSection" / theDate bindToClient httpClient

  val theCall = client(theWeather --> "sunny", body --> Form(gender --> "male"), theDate --> LocalDate.of(2015, 1, 1), theUser --> System.getenv("USER"))

  val response = Await.result(theCall)

  println("Response headers: " + headersFrom(response))
  println("Response: " + statusAndContentFrom(response))
}
