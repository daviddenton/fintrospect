package io.fintrospect.formats

import java.io.OutputStream

import com.twitter.finagle.http.Response
import com.twitter.util.Future
import io.fintrospect.ContentType
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}
import org.jboss.netty.util.CharsetUtil._

import scala.language.implicitConversions

/**
 * Builds Http Responses using a particular custom format object. Plugs into the rest of the library.
 * @param toFormat renders the custom format object to a string
 * @param errorFormat renders an error message as the custom format
 * @param exceptionFormat renders an exception as the custom format
 * @param contentType the content type to return in all responses
 * @tparam T The custom format object type
 */
class ResponseBuilder[T](toFormat: T => String,
                         errorFormat: String => T,
                         exceptionFormat: Throwable => T,
                         contentType: ContentType) {
  private val response = Response()

  def withError(e: Throwable) = withContent(exceptionFormat(e))

  def withErrorMessage(e: String) = withContent(errorFormat(e))

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

  def withContent(channelBuffer: ChannelBuffer): ResponseBuilder[T] = {
    response.setContentType(contentType.value)
    response.setContent(channelBuffer)
    this
  }

  def withContent(f: OutputStream => Unit): ResponseBuilder[T] = {
    response.setContentType(contentType.value)
    response.withOutputStream(f)
    this
  }

  def withHeaders(headers: (String, String)*): ResponseBuilder[T] = {
    headers.map{case (name: String, value: String) => response.headers().add(name, value)}
    this
  }

  def build: Response = response

  def toFuture: Future[Response] = Future.value(build)
}

object ResponseBuilder {
  implicit def toFuture(builder: ResponseBuilder[_]): Future[HttpResponse] = builder.toFuture

  implicit def toFuture(response: HttpResponse): Future[HttpResponse] = Future.value(response)
}

