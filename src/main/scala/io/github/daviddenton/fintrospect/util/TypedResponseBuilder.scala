package io.github.daviddenton.fintrospect.util

import io.github.daviddenton.fintrospect.ContentType
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}

import scala.language.implicitConversions

class TypedResponseBuilder[T](toFormat: T => String,
                              contentType: ContentType,
                              msgToError: String => T,
                              exToError: Throwable => T,
                              bpToError: List[RequestParameter[_]] => T) {

  def apply(): ResponseBuilder[T] = new ResponseBuilder[T](toFormat, contentType)

  def Ok: HttpResponse = apply().withCode(HttpResponseStatus.OK).build

  def Ok(content: T): HttpResponse = Ok(toFormat(content))

  def Ok(content: String): HttpResponse = apply().withCode(HttpResponseStatus.OK).withContent(content).build

  def Error(status: HttpResponseStatus, message: String): HttpResponse = apply().withCode(status).withContent(toFormat(msgToError(message))).build

  def Error(status: HttpResponseStatus, content: T): HttpResponse = apply().withCode(status).withContent(toFormat(content)).build

  def Error(status: HttpResponseStatus, error: Throwable): HttpResponse = Error(status, exToError(error))

  def BadRequest(badParameters: List[RequestParameter[_]]): HttpResponse = Error(HttpResponseStatus.BAD_REQUEST, bpToError(badParameters))
}
