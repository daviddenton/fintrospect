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

object ResponseBuilder {

  implicit def toFuture(builder: ResponseBuilder[_]): Future[HttpResponse] = builder.toFuture

  implicit def toFuture(response: HttpResponse): Future[HttpResponse] = Future.value(response)

  def Json = new TypedResponseBuilder[JsonRootNode](
    () => new ResponseBuilder[JsonRootNode](new PrettyJsonFormatter().format, ContentTypes.APPLICATION_JSON),
    errorMessage => obj("message" -> string(errorMessage)),
    throwable => string(Option(throwable.getMessage).getOrElse(throwable.getClass.getName)).asInstanceOf[JsonRootNode],
    badParameters => {
      val messages = badParameters.map(p => obj(
        "name" -> string(p.name),
        "type" -> string(p.where),
        "datatype" -> string(p.paramType.name),
        "required" -> boolean(p.requirement.required)
      ))

      ArgoUtil.obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages))
    })

}