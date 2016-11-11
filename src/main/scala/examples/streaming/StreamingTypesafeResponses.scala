package examples.streaming

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.fintrospect.formats.Xml.ResponseBuilder.implicits._
import io.fintrospect.{ModuleSpec, RouteSpec}

/**
  * This example shows how to build a server and endpoint which uses twitter's AsyncStream
  * and Fintrospect typesafe ResponseBuilders to stream responses to a client.
  */
object StreamingTypesafeResponses extends App {

  val infiniteTypesafeStream = AsyncStream.fromSeq(Stream.from(0).map(i => <hello> {i} </hello>))

  val streamOfXml = Service.mk[Request, Response] { req => Ok(infiniteTypesafeStream) }

  val svc = ModuleSpec(Root).withRoute(RouteSpec().at(Get) bindTo streamOfXml).toService

  println("run: \"curl -v http://localhost:9000\" to get infinite chunks of streamed output XML")

  Await.result(Http.server.withStreaming(enabled = true).serve(":9000", svc))
}
