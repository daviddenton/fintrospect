package examples.clients

import java.time.LocalDate

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.fintrospect.RouteSpec
import io.fintrospect.formats.PlainText.ResponseBuilder.implicits.{responseBuilderToFuture, statusToResponseBuilderConfig}
import io.fintrospect.parameters.{Body, Form, FormField, Header, Path, Query}
import io.fintrospect.testing.TestHttpServer
import io.fintrospect.util.HttpRequestResponseUtil.{headersFrom, statusAndContentFrom}

/**
  * Simple example of how to define client endpoints using the same techniques as the server routes.
  * Note that the client will automatically reject (with a 400) any unknown or missing parameters, as per the
  * specified route. The response is also decorated with the anonymised route, allowing for collection of
  * metrics about timing and number of requests going to the downsteam systems.
  *
  * This example also shows how you can re-use the RouteSpec across client and servers, thus allowing really simple
  * stub/fake implementations of remote systems to be created.
  */
object ClientSideAndSharedRouteSpecExample extends App {

  val theDate = Path.localDate("date")
  val theWeather = Query.optional.string("weather")
  val theUser = Header.required.string("user")
  val gender = FormField.optional.string("gender")
  val body = Body.form(gender)

  val sharedRouteSpec = RouteSpec()
    .taking(theUser)
    .taking(theWeather)
    .body(body)
    .at(Get) / "firstSection" / theDate

  val fakeServerRoute = sharedRouteSpec bindTo (dateFromPath => Service.mk[Request, Response] {
    request: Request => {
      println("URL was " + request.uri)
      println("Headers were " + headersFrom(request))
      println("Form sent was " + (body <-- request))
      println("Date send was " + dateFromPath.toString)
      Ok(dateFromPath.toString)
    }
  })

  Await.result(new TestHttpServer(10000, fakeServerRoute).start())

  val client = sharedRouteSpec bindToClient Http.newService("localhost:10000")

  val theCall = client(theWeather --> Option("sunny"), body --> Form(gender --> "male"), theDate --> LocalDate.of(2015, 1, 1), theUser --> System.getenv("USER"))

  val response = Await.result(theCall)

  println("Response headers: " + headersFrom(response))
  println("Response: " + statusAndContentFrom(response))
}
