package io.fintrospect.formats

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.io.Buf
import org.jboss.netty.buffer.ChannelBuffer

/**
 * Convienience methods for building Http Responses
 */
trait ResponseBuilderMethods[T] {
  def HttpResponse(): ResponseBuilder[T]

  def HttpResponse(code: Status): ResponseBuilder[T] = HttpResponse().withCode(code)

  def OK: Response = HttpResponse(Ok).build

  def OK(content: ChannelBuffer): Response = HttpResponse(Ok).withContent(content).build

  def OK(content: Buf): Response = HttpResponse(Ok).withContent(content).build

  def OK(content: T): Response = HttpResponse(Ok).withContent(content).build

  def OK(content: String): Response = HttpResponse(Ok).withContent(content).build

  def Error(code: Status, content: ChannelBuffer): Response = HttpResponse(code).withContent(content).build

  def Error(code: Status, content: Buf): Response = HttpResponse(code).withContent(content).build

  def Error(code: Status, content: T): Response = HttpResponse(code).withContent(content).build

  def Error(code: Status, message: String): Response = HttpResponse(code).withErrorMessage(message).build

  def Error(code: Status, error: Throwable): Response = HttpResponse(code).withError(error).build
}
