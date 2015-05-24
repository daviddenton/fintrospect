package io.github.daviddenton.fintrospect.util

import io.github.daviddenton.fintrospect.parameters.RequestParameter
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}

import scala.language.implicitConversions

class TypedResponseBuilder[T](newBuilder: () => ResponseBuilder[T],
                              msgToError: String => T,
                              exToError: Throwable => T,
                              bpToError: List[RequestParameter[_]] => T) {

  def apply(): ResponseBuilder[T] = newBuilder()

  def Ok: HttpResponse = apply().withCode(OK).build

  def Ok(content: T): HttpResponse = apply().withCode(OK).withContent(content).build

  def Ok(content: String): HttpResponse = apply().withCode(OK).withContent(content).build

  def Error(status: HttpResponseStatus, message: String): HttpResponse = apply().withCode(status).withContent(msgToError(message)).build

  def Error(status: HttpResponseStatus, content: T): HttpResponse = apply().withCode(status).withContent(content).build

  def Error(status: HttpResponseStatus, error: Throwable): HttpResponse = Error(status, exToError(error))

  def BadRequest(badParameters: List[RequestParameter[_]]): HttpResponse = Error(BAD_REQUEST, bpToError(badParameters))
}
