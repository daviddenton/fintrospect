package io.fintrospect.formats

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Response, Status}
import com.twitter.io.{Buf, Reader}
import org.jboss.netty.buffer.ChannelBuffer

import scala.language.implicitConversions

/**
 * Convenience methods for building Http Responses
 */
trait AbstractResponseBuilder[T] {

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
  implicit def statusToResponseBuilderConfig(status: Status): ResponseBuilderConfig = new ResponseBuilderConfig(status)

  implicit def responseBuilderToResponse(builder: ResponseBuilder[T]): Response = builder.build()

  def HttpResponse(): ResponseBuilder[T]

  def HttpResponse(status: Status): ResponseBuilder[T] = HttpResponse().withCode(status)

  def OK: Response = HttpResponse(Ok)

  def OK(channelBuffer: ChannelBuffer) = statusToResponseBuilderConfig(Ok)(channelBuffer)

  def OK(reader: Reader) = statusToResponseBuilderConfig(Ok)(reader)

  def OK(buf: Buf) = statusToResponseBuilderConfig(Ok)(buf)

  def OK(content: T) = statusToResponseBuilderConfig(Ok)(content)

  def OK(content: String) = statusToResponseBuilderConfig(Ok)(content)

  def Error(status: Status, reader: Reader) = statusToResponseBuilderConfig(status)(reader)

  def Error(status: Status) = statusToResponseBuilderConfig(status)("")

  def Error(status: Status, channelBuffer: ChannelBuffer) = statusToResponseBuilderConfig(status)(channelBuffer)

  def Error(status: Status, buf: Buf) = statusToResponseBuilderConfig(status)(buf)

  def Error(status: Status, t: T) = statusToResponseBuilderConfig(status)(t)

  def Error(status: Status, message: String) = statusToResponseBuilderConfig(status)().withErrorMessage(message)

  def Error(status: Status, error: Throwable) = statusToResponseBuilderConfig(status)().withError(error).build()
}
