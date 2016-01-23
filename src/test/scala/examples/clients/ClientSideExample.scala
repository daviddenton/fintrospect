package examples.clients

import java.time.LocalDate

import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.http.Status._
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.fintrospect.RouteSpec
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.parameters._
import io.fintrospect.util.HttpRequestResponseUtil._

/**
  * Simple example of how to define client endpoints using the same techniques as the server routes.
  * Note that the client will automatically reject (with a 400) any unknown or missing parameters, as per the
  * specified route. The response is also decorated with the anonymised route, allowing for collection of
  * metrics about timing and number of requests going to the downsteam systems.
  */
object ClientSideExample extends App {

  Http.serve(":10000", Service.mk[Request, Response] {
    request => {
      println("URL was " + request.uri)
      println("Headers were " + headersFrom(request))
      println("Content was " + contentFrom(request))
      Ok("")
    }
  })

  val httpClient = Http.newService("localhost:10000")

  val theDate = Path.localDate("date")
  val theWeather = Query.optional.string("weather")
  val theUser = Header.required.string("user")
  val gender = FormField.optional.string("gender")
  val body = Body.form(gender)

  val client = RouteSpec()
    .taking(theUser)
    .taking(theWeather)
    .body(body)
    .at(Get) / "firstSection" / theDate bindToClient httpClient

  val theCall = client(theWeather --> Option("sunny"), body --> Form(gender --> "male"), theDate --> LocalDate.of(2015, 1, 1), theUser --> System.getenv("USER"))

  val response = Await.result(theCall)

  println("Response headers: " + headersFrom(response))
  println("Response: " + statusAndContentFrom(response))
}
