package io.fintrospect.formats

import java.io.OutputStream

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Response, Status}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffer

import scala.language.implicitConversions

/**
 * Convenience methods for building Http Responses
 */
trait AbstractResponseBuilder[T] {

  object implicits {
    /**
      * Implicitly convert a ResponseBuilder object to a Future[Response]
      */
    implicit def responseBuilderToFuture(builder: ResponseBuilder[T]): Future[Response] = builder.toFuture

    /**
      * Implicitly convert a Response object to a Future[Response]
      */
    implicit def responseToFuture(response: Response): Future[Response] = Future.value(response)

    /**
      * Implicitly convert a Status object to a correctly generified ResponseBuilderConfig
      */
    implicit def statusToResponseBuilderConfig(status: Status): ResponseBuilderConfig = new ResponseBuilderConfig(status)

    /**
      * Implicitly convert a ResponseBuilder object to a Response
      */
    implicit def responseBuilderToResponse(builder: ResponseBuilder[T]): Response = builder.build()
  }

  /**
   * Intermediate object used when constructing a ResponseBuilder from a Status object (via implicit conversion)
   */
  class ResponseBuilderConfig(status: Status) {
    def apply(): ResponseBuilder[T] = HttpResponse(status)

    def apply(f: OutputStream => Unit): ResponseBuilder[T] = HttpResponse(status).withContent(f)

    def apply(channelBuffer: ChannelBuffer): ResponseBuilder[T] = HttpResponse(status).withContent(channelBuffer)

    def apply(content: String): ResponseBuilder[T] = if (status.code < 400) HttpResponse(status).withContent(content) else HttpResponse(status).withErrorMessage(content)

    def apply(buf: Buf): ResponseBuilder[T] = HttpResponse(status).withContent(buf)

    def apply(stream: AsyncStream[T]): ResponseBuilder[T] = HttpResponse(status).withContent(stream)

    def apply(reader: Reader): ResponseBuilder[T] = HttpResponse(status).withContent(reader)

    def apply(t: T): ResponseBuilder[T] = HttpResponse(status).withContent(t)

    def apply(error: Throwable): ResponseBuilder[T] = HttpResponse(status).withError(error)
  }

  def HttpResponse(): ResponseBuilder[T]

  def HttpResponse(status: Status): ResponseBuilder[T] = HttpResponse().withCode(status)

  def OK: Response = HttpResponse(Ok).build()

  def OK(channelBuffer: ChannelBuffer) = implicits.statusToResponseBuilderConfig(Ok)(channelBuffer)

  def OK(reader: Reader) = implicits.statusToResponseBuilderConfig(Ok)(reader)

  def OK(buf: Buf) = implicits.statusToResponseBuilderConfig(Ok)(buf)

  def OK(content: T) = implicits.statusToResponseBuilderConfig(Ok)(content)

  def OK(content: String) = implicits.statusToResponseBuilderConfig(Ok)(content)

  def Error(status: Status, reader: Reader) = implicits.statusToResponseBuilderConfig(status)(reader)

  def Error(status: Status) = implicits.statusToResponseBuilderConfig(status)("")

  def Error(status: Status, channelBuffer: ChannelBuffer) = implicits.statusToResponseBuilderConfig(status)(channelBuffer)

  def Error(status: Status, buf: Buf) = implicits.statusToResponseBuilderConfig(status)(buf)

  def Error(status: Status, t: T) = implicits.statusToResponseBuilderConfig(status)(t)

  def Error(status: Status, message: String) = implicits.statusToResponseBuilderConfig(status)().withErrorMessage(message)

  def Error(status: Status, error: Throwable) = implicits.statusToResponseBuilderConfig(status)().withError(error).build()
}
