+++
title = "msgpack"
tags = ["msgpack", "message format"]
categories = ["recipe"]
intro = ""
+++

```scala
object MsgPack_Example extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.Request
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.result
  import com.twitter.util.Future
  import io.fintrospect.formats.MsgPack.Auto._
  import io.fintrospect.formats.MsgPack.Format.{decode, encode}
  import io.fintrospect.{RouteModule, RouteSpec}

  case class StreetAddress(address: String)

  case class Letter(to: StreetAddress, from: StreetAddress, message: String)

  val replyToLetter = RouteSpec()
    .at(Post) / "reply" bindTo InOut[StreetAddress, Letter](
    Service.mk { in: StreetAddress =>
      Future(Letter(StreetAddress("2 Bob St"), in, "hi fools!"))
    })

  val module = RouteModule(Root).withRoute(replyToLetter)

  Http.serve(":8181", module.toService)


  val request = Request(Post, "reply")
  request.content = encode(StreetAddress("1 hello street"))
  val response = result(Http.newService("localhost:8181")(request))
  println("Response was:" + decode[Letter](response.content))
}
```