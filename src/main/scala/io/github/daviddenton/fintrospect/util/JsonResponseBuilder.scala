package io.github.daviddenton.fintrospect.util

import argo.format.PrettyJsonFormatter
import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.ContentTypes
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}

class JsonResponseBuilder extends ResponseBuilder[JsonRootNode](
  new PrettyJsonFormatter().format,
  errorMessage => obj("message" -> string(errorMessage)),
  throwable => string(Option(throwable.getMessage).getOrElse(throwable.getClass.getName)).asInstanceOf[JsonRootNode],
  ContentTypes.APPLICATION_JSON)

object JsonResponseBuilder {

  def Response() = apply()

  def Ok: HttpResponse = apply().withCode(OK).build

  def Ok(content: JsonRootNode): HttpResponse = apply().withCode(OK).withContent(content).build

  def Ok(content: String): HttpResponse = apply().withCode(OK).withContent(content).build

  def Error(status: HttpResponseStatus, content: JsonRootNode): HttpResponse = apply().withCode(status).withContent(content).build

  def Error(status: HttpResponseStatus, message: String): HttpResponse = apply().withCode(status).withErrorMessage(message).build

  def Error(status: HttpResponseStatus, error: Throwable): HttpResponse = apply().withCode(status).withError(error).build

  // bin?!?!
  def apply() = new JsonResponseBuilder()
}