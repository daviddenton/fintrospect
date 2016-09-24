package io.fintrospect.formats

import java.io.OutputStream
import java.nio.charset.StandardCharsets.UTF_8

import com.twitter.finagle.http.{Cookie, Response, Status}
import com.twitter.io.Buf.Utf8
import com.twitter.io.{Buf, Reader}
import com.twitter.util.Future
import io.fintrospect.ContentType
import io.fintrospect.util.Builder
import org.jboss.netty.buffer.ChannelBuffer

import scala.language.implicitConversions

/**
  * Builds Http Responses using a particular custom format object. Plugs into the rest of the library.
  * @param toFormat renders the custom format object to a string
  * @param errorFormat renders an error message as the custom format
  * @param exceptionFormat renders an exception as the custom format
  * @param contentType the content type to return in all responses
  * @tparam T The custom format object type
  */
class ResponseBuilder[T](toFormat: T => Buf, errorFormat: String => T,
                         exceptionFormat: Throwable => T,
                         contentType: ContentType) extends Builder[Response] {

  private var response = Response()

  def withError(e: Throwable) = withContent(exceptionFormat(e))

  def withErrorMessage(e: String) = withContent(errorFormat(e))

  def withCode(status: Status): ResponseBuilder[T] = {
    response.setStatusCode(status.code)
    this
  }

  def withContent(content: T): ResponseBuilder[T] = withContent(toFormat(content))

  def withContent(content: String): ResponseBuilder[T] = {
    response.setContentString(content)
    this
  }

  def withContent(reader: Reader): ResponseBuilder[T] = {
    val newResponse = Response(response.version, response.status, reader)
    response.headerMap.foreach(kv => newResponse.headerMap.add(kv._1, kv._2))
    response = newResponse
    this
  }

  def withContent(buf: Buf): ResponseBuilder[T] = {
    response.content = buf
    this
  }

  def withContent(channelBuffer: ChannelBuffer): ResponseBuilder[T] = {
    response.setContentString(channelBuffer.toString(UTF_8))
    this
  }

  def withContent(f: OutputStream => Unit): ResponseBuilder[T] = {
    response.withOutputStream(f)
    this
  }

  def withCookies(cookies: Cookie*): ResponseBuilder[T] = {
    cookies.foreach { response.cookies.add }
    this
  }

  def withHeaders(headers: (String, String)*): ResponseBuilder[T] = {
    headers.map { case (name: String, value: String) => response.headerMap.add(name, value) }
    this
  }

  override def build(): Response = {
    response.setContentType(contentType.value)
    response
  }

  def toFuture: Future[Response] = Future.value(build())
}

/**
  * Generic ResponseBuilder support
  */
object ResponseBuilder {

  private case class HIDDEN(value: String)

  def HttpResponse(contentType: ContentType): ResponseBuilder[_] = new ResponseBuilder[HIDDEN](s => Utf8(s.value), HIDDEN, e => HIDDEN(e.getMessage), contentType)

  implicit def responseBuilderToFuture(builder: ResponseBuilder[_]): Future[Response] = builder.toFuture

  implicit def responseToFuture(response: Response): Future[Response] = Future.value(response)
}

