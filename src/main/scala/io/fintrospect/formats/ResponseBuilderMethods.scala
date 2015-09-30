package io.fintrospect.formats

import org.jboss.netty.handler.codec.http.HttpResponseStatus.OK
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}

/**
 * Convienience methods for building Http Responses
 */
trait ResponseBuilderMethods[T] {
  def Response(): ResponseBuilder[T]

  def Response(code: HttpResponseStatus): ResponseBuilder[T] = Response().withCode(code)

  def Ok: HttpResponse = Response(OK).build

  def Ok(content: T): HttpResponse = Response(OK).withContent(content).build

  def Ok(content: String): HttpResponse = Response(OK).withContent(content).build

  def Error(code: HttpResponseStatus, content: T): HttpResponse = Response(code).withContent(content).build

  def Error(code: HttpResponseStatus, message: String): HttpResponse = Response(code).withErrorMessage(message).build

  def Error(code: HttpResponseStatus, error: Throwable): HttpResponse = Response(code).withError(error).build
}
