package io.github.daviddenton.fintrospect.util

import argo.format.PrettyJsonFormatter
import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.ContentTypes
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}

object JsonResponseBuilder {

  private val formatJson: JsonRootNode => String = new PrettyJsonFormatter().format
  private val formatErrorMessage: String => JsonRootNode = errorMessage => obj("message" -> string(errorMessage))

  private def formatError: Throwable => JsonRootNode = throwable => string(Option(throwable.getMessage).getOrElse(throwable.getClass.getName)).asInstanceOf[JsonRootNode]

  def Response(): ResponseBuilder[JsonRootNode] = new ResponseBuilder(formatJson, formatErrorMessage, formatError, ContentTypes.APPLICATION_JSON)

  def Response(code: HttpResponseStatus): ResponseBuilder[JsonRootNode] = Response().withCode(code)

  def Ok: HttpResponse = Response(OK).build

  def Ok(content: JsonRootNode): HttpResponse = Response(OK).withContent(content).build

  def Ok(content: String): HttpResponse = Response(OK).withContent(content).build

  def Error(code: HttpResponseStatus, content: JsonRootNode): HttpResponse = Response(code).withContent(content).build

  def Error(code: HttpResponseStatus, message: String): HttpResponse = Response(code).withErrorMessage(message).build

  def Error(code: HttpResponseStatus, error: Throwable): HttpResponse = Response(code).withError(error).build
}