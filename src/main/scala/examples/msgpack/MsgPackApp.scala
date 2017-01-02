package examples.msgpack

import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import io.fintrospect.ContentTypes.APPLICATION_X_MSGPACK
import io.fintrospect.formats.MsgPack.Filters._
import io.fintrospect.formats.MsgPack.ResponseBuilder._
import io.fintrospect.formats.MsgPack.{Filters, bodySpec}
import io.fintrospect.formats.{MsgPack, MsgPackMsg}
import io.fintrospect.parameters.Body
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{RouteModule, RouteSpec}

case class StreetAddress(address: String)

case class Letter(to: StreetAddress, from: StreetAddress, message: String)

/**
  * This example uses MsgPack, which is a binary format.
  */
object MsgPackApp extends App {

  // manually encodes the response as a MsgPack object
  val viewLetterRoute = RouteSpec("returns a letter instance in MsgPack format")
    .producing(APPLICATION_X_MSGPACK)
    .at(Get) / "letter" bindTo
    Service.mk[Request, Response] {
      r => Ok(MsgPackMsg(Letter(StreetAddress("2 Bob St"), StreetAddress("20 Rita St"), "hi fools!")))
    }

  // using AutoFilters, receives an address and then responds with a letter
  val replyToLetter = RouteSpec("send your address and we'll send you back a letter!")
    .consuming(APPLICATION_X_MSGPACK)
    .producing(APPLICATION_X_MSGPACK)
    .at(Post) / "reply" bindTo Filters.AutoInOut[StreetAddress, Letter](Body(bodySpec[StreetAddress]())).andThen(Service.mk { in: StreetAddress =>
    Future(Letter(StreetAddress("2 Bob St"), in, "hi fools!"))
  })

  val module = RouteModule(Root, SimpleJson()).withRoute(viewLetterRoute).withRoute(replyToLetter)

  println("See the service description at: http://localhost:8181")
  Await.ready(
    Http.serve(":8181", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))
  )
}

/**
  * Run this client to use the above "reply" service... obviously you can't send this in the browser as it's a binary format.
  */
object MsgPackClient extends App {
  val request = Request(Post, "reply")
  request.content = MsgPack.Format.encode(StreetAddress("1 hello street"))

  val response = Await.result(Http.newService("localhost:8181")(request))
  println(MsgPack.Format.decode[Letter](response.content))
}