package io.github.daviddenton.fintrospect.util

import argo.format.PrettyJsonFormatter
import argo.jdom.JsonRootNode
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.{ContentType, ContentTypes}
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}
import org.jboss.netty.util.CharsetUtil._

import scala.language.implicitConversions

class ResponseBuilder[T](toFormat: T => String, contentType: ContentType) {
  private val response = Response()

  def withCode(code: HttpResponseStatus): ResponseBuilder[T] = {
    response.setStatus(code)
    this
  }

  def withContent(content: T): ResponseBuilder[T] = withContent(toFormat(content))

  def withContent(content: String): ResponseBuilder[T] = {
    response.setContentType(contentType.value)
    response.setContent(copiedBuffer(content, UTF_8))
    this
  }

  def build: Response = response

  def toFuture: Future[Response] = Future.value(build)
}

class TypedRespBuilder[T](toFormat: T => String, msgToError: String => T, exToError: Throwable => T, private val contentType: ContentType) {

  def apply(): ResponseBuilder[T] = new ResponseBuilder[T](toFormat, contentType)

  def Ok: ResponseBuilder[T] = apply().withCode(HttpResponseStatus.OK)

  def Ok(content: T): ResponseBuilder[T] = Ok(toFormat(content))

  def Ok(content: String): ResponseBuilder[T] = Ok.withContent(content)

  def Error(status: HttpResponseStatus, message: String): ResponseBuilder[T] = apply().withCode(status).withContent(toFormat(msgToError(message)))

  def Error(status: HttpResponseStatus, content: T): ResponseBuilder[T] = apply().withCode(status).withContent(toFormat(content))

  def Error(status: HttpResponseStatus, error: Throwable): ResponseBuilder[T] = apply().withCode(status).withContent(toFormat(exToError(error)))
}

object ResponseBuilder {

  implicit def toFuture(builder: ResponseBuilder[_]): Future[HttpResponse] = builder.toFuture

  private val jsonFormatter = new PrettyJsonFormatter()

  def Json = new TypedRespBuilder[JsonRootNode](
    jsonFormatter.format,
    m => obj("message" -> string(m)),
    t => obj("message" -> string(Option(t.getMessage).getOrElse(t.getClass.getName))),
    ContentTypes.APPLICATION_JSON)
}