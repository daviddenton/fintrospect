package io.fintrospect.formats

import com.twitter.finagle.httpx.{Response, Status}

/**
 * Convienience methods for building Http Responses
 */
trait ResponseBuilderMethods[T] {
  def HttpResponse(): ResponseBuilder[T]

  def HttpResponse(code: Status): ResponseBuilder[T] = HttpResponse().withCode(code)

  @deprecated("use HttpResponse() instead to avoid name clashes with Finagle library changes", "v10.0.0")
  def Response(): ResponseBuilder[T] = HttpResponse()

  @deprecated("use HttpResponse() instead to avoid name clashes with Finagle library changes", "v10.0.0")
  def Response(code: Status): ResponseBuilder[T] = HttpResponse().withCode(code)

  @deprecated("use OK() instead to avoid name clashes with Finagle library changes", "v10.0.0")
  def Ok: Response = HttpResponse(Status.Ok).build

  @deprecated("use OK() instead to avoid name clashes with Finagle library changes", "v10.0.0")
  def Ok(content: T): Response = HttpResponse(Status.Ok).withContent(content).build

  @deprecated("use OK() instead to avoid name clashes with Finagle library changes", "v10.0.0")
  def Ok(content: String): Response = HttpResponse(Status.Ok).withContent(content).build

  def OK: Response = HttpResponse(Status.Ok).build

  def OK(content: T): Response = HttpResponse(Status.Ok).withContent(content).build

  def OK(content: String): Response = HttpResponse(Status.Ok).withContent(content).build

  def Error(code: Status, content: T): Response = HttpResponse(code).withContent(content).build

  def Error(code: Status, message: String): Response = HttpResponse(code).withErrorMessage(message).build

  def Error(code: Status, error: Throwable): Response = HttpResponse(code).withError(error).build
}
