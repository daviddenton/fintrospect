package io.fintrospect.formats

import java.io.OutputStream
import java.nio.charset.StandardCharsets.UTF_8

import com.google.common.net.HttpHeaders
import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.{Cookie, Response, Status}
import com.twitter.io.Buf.Utf8
import com.twitter.io.{Buf, Reader}
import com.twitter.util.Future
import io.fintrospect.ContentType
import org.jboss.netty.buffer.ChannelBuffer

import scala.language.implicitConversions

/**
  * Builds Http Responses using a particular custom format object. Plugs into the rest of the library.
  *
  * @param toFormat        renders the custom format object to a string
  * @param errorFormat     renders an error message as the custom format
  * @param exceptionFormat renders an exception as the custom format
  * @param contentType     the content type to return in all responses
  * @tparam T The custom format object type
  */
class ResponseBuilder[T](toFormat: T => Buf,
                         errorFormat: String => T,
                         exceptionFormat: Throwable => T,
                         contentType: ContentType) {

  private[formats] var response = Response()

  def withError(e: Throwable): ResponseBuilder[T] = withContent(exceptionFormat(e))

  def withErrorMessage(e: String): ResponseBuilder[T] = withContent(errorFormat(e))

  def withContent(content: T): ResponseBuilder[T] = withContent(toFormat(content))

  def withContent(buf: Buf): ResponseBuilder[T] = {
    response.content = buf
    this
  }

  def withCode(status: Status): ResponseBuilder[T] = {
    response.setStatusCode(status.code)
    this
  }

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

  def withContent(stream: AsyncStream[T]): ResponseBuilder[T] = {
    val newResponse = Response(response.version, response.status)
    response.headerMap.foreach(kv => newResponse.headerMap.add(kv._1, kv._2))
    newResponse.setChunked(true)
    val writable = newResponse.writer
    stream.foreachF(chunk => writable.write(toFormat(chunk))).ensure(writable.close())
    response = newResponse
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
    cookies.foreach {
      response.cookies.add
    }
    this
  }

  def withHeaders(headers: (String, String)*): ResponseBuilder[T] = {
    headers.map { case (name: String, value: String) => response.headerMap.add(name, value) }
    this
  }

  def toFuture: Future[Response] = Future(build())

  def build(): Response = {
    response.setContentType(contentType.value)
    response
  }
}

/**
  * Generic ResponseBuilder support
  */
object ResponseBuilder {

  /**
    * Convenience method to create an HTTP Redirect to the passed location. Defaults to HTTP status 302 (overridable)
    */
  def RedirectTo(newLocation: String, status: Status = Status.SeeOther): Response = {
    val response = Response(status)
    response.location = newLocation
    response
  }

  def HttpResponse(contentType: ContentType): ResponseBuilder[_] = new ResponseBuilder[HIDDEN](s => Utf8(s.value), HIDDEN, e => HIDDEN(e.getMessage), contentType)

  private case class HIDDEN(value: String)

  implicit def responseBuilderToResponse(builder: ResponseBuilder[_]): Response = builder.build()

  implicit def responseBuilderToFuture(builder: ResponseBuilder[_]): Future[Response] = builder.toFuture

  implicit def responseToFuture(response: Response): Future[Response] = Future(response)
}

