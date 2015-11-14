package io.fintrospect.formats

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.io.Buf
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

    def apply(content: ChannelBuffer): ResponseBuilder[T] = HttpResponse(status).withContent(content)

    def apply(content: String): ResponseBuilder[T] = if (status.code < 400) HttpResponse(status).withContent(content) else HttpResponse(status).withErrorMessage(content)

    def apply(content: Buf): ResponseBuilder[T] = HttpResponse(status).withContent(content)

    def apply(content: T): ResponseBuilder[T] = HttpResponse(status).withContent(content)

    def apply(error: Throwable): ResponseBuilder[T] = HttpResponse(status).withError(error)
  }

  /**
   * Implicitly convert a Status object implicitly to a correctly generified ResponseBuilderConfig, and from there to a ResponseBuilder
   */
  implicit def toResponseBuilder(status: Status): ResponseBuilderConfig = new ResponseBuilderConfig(status)

  implicit def toResponse(builder: ResponseBuilder[T]): Response = builder.build

  def HttpResponse(): ResponseBuilder[T]

  def HttpResponse(code: Status): ResponseBuilder[T] = HttpResponse().withCode(code)

  def OK: Response = HttpResponse(Ok)

  def OK(content: ChannelBuffer) = toResponseBuilder(Ok)(content)

  def OK(content: Buf) = toResponseBuilder(Ok)(content)

  def OK(content: T) = toResponseBuilder(Ok)(content)

  def OK(content: String) = toResponseBuilder(Ok)(content)

  def Error(code: Status, content: ChannelBuffer) = toResponseBuilder(code)(content)

  def Error(code: Status, content: Buf) = toResponseBuilder(code)(content)

  def Error(code: Status, content: T) = toResponseBuilder(code)(content)

  def Error(code: Status, message: String) = toResponseBuilder(code)().withErrorMessage(message)

  def Error(code: Status, error: Throwable) = toResponseBuilder(code)().withError(error).build
}
