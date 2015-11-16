package io.fintrospect.formats

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.io.{Buf, Reader}
import org.jboss.netty.buffer.ChannelBuffer

import scala.language.implicitConversions

/**
 * Convenience methods for building Http Responses
 */
trait ResponseBuilderMethods[T] {

  /**
   * Intermediate object used when constructing a ResponseBuilder from a Status object (via implicit conversion)
   */
  class ResponseBuilderConfig(status: Status) {
    def apply(): ResponseBuilder[T] = HttpResponse(status)

    def apply(channelBuffer: ChannelBuffer): ResponseBuilder[T] = HttpResponse(status).withContent(channelBuffer)

    def apply(content: String): ResponseBuilder[T] = if (status.code < 400) HttpResponse(status).withContent(content) else HttpResponse(status).withErrorMessage(content)

    def apply(buf: Buf): ResponseBuilder[T] = HttpResponse(status).withContent(buf)

    def apply(reader: Reader): ResponseBuilder[T] = HttpResponse(status).withContent(reader)

    def apply(t: T): ResponseBuilder[T] = HttpResponse(status).withContent(t)

    def apply(error: Throwable): ResponseBuilder[T] = HttpResponse(status).withError(error)
  }

  /**
   * Implicitly convert a Status object implicitly to a correctly generified ResponseBuilderConfig, and from there to a ResponseBuilder
   */
  implicit def toResponseBuilder(status: Status): ResponseBuilderConfig = new ResponseBuilderConfig(status)

  implicit def toResponse(builder: ResponseBuilder[T]): Response = builder.build

  def HttpResponse(): ResponseBuilder[T]

  def HttpResponse(status: Status): ResponseBuilder[T] = HttpResponse().withCode(status)

  def OK: Response = HttpResponse(Ok)

  def OK(channelBuffer: ChannelBuffer) = toResponseBuilder(Ok)(channelBuffer)

  def OK(reader: Reader) = toResponseBuilder(Ok)(reader)

  def OK(buf: Buf) = toResponseBuilder(Ok)(buf)

  def OK(content: T) = toResponseBuilder(Ok)(content)

  def OK(content: String) = toResponseBuilder(Ok)(content)

  def Error(status: Status, reader: Reader) = toResponseBuilder(status)(reader)

  def Error(status: Status, channelBuffer: ChannelBuffer) = toResponseBuilder(status)(channelBuffer)

  def Error(status: Status, buf: Buf) = toResponseBuilder(status)(buf)

  def Error(status: Status, t: T) = toResponseBuilder(status)(t)

  def Error(status: Status, message: String) = toResponseBuilder(status)().withErrorMessage(message)

  def Error(status: Status, error: Throwable) = toResponseBuilder(status)().withError(error).build
}
