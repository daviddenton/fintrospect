package io.fintrospect.formats

import com.twitter.finagle.httpx.{Response, Status}

/**
 * Convienience methods for building Http Responses
 */
trait ResponseBuilderMethods[T] {
  def Response(): ResponseBuilder[T]

  def Response(code: Status): ResponseBuilder[T] = Response().withCode(code)

  def Ok: Response = Response(Status.Ok).build

  def Ok(content: T): Response = Response(Status.Ok).withContent(content).build

  def Ok(content: String): Response = Response(Status.Ok).withContent(content).build

  def Error(code: Status, content: T): Response = Response(code).withContent(content).build

  def Error(code: Status, message: String): Response = Response(code).withErrorMessage(message).build

  def Error(code: Status, error: Throwable): Response = Response(code).withError(error).build
}
