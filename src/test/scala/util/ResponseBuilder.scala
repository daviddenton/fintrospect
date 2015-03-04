package util

import argo.format.PrettyJsonFormatter
import argo.jdom.JsonNodeFactories.{`object` => obj, _}
import argo.jdom.JsonRootNode
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.util.CharsetUtil._
import util.ResponseBuilder.formatter

object ResponseBuilder {
  implicit def toFuture(builder: ResponseBuilder): Future[Response] = builder.toFuture

  private val formatter = new PrettyJsonFormatter()

  def apply(): ResponseBuilder = new ResponseBuilder()

  def Ok = new ResponseBuilder().withCode(HttpResponseStatus.OK)

  def Ok(content: String) = new ResponseBuilder().withCode(HttpResponseStatus.OK).withContent(content)

  def Ok(content: JsonRootNode) = new ResponseBuilder()
    .withCode(HttpResponseStatus.OK).withContent(content)

  def Error(status: HttpResponseStatus, error: Throwable) = {
    new ResponseBuilder().withCode(status).withContent(formatter.format(errorToJson(error)))
  }

  def errorToJson(error: Throwable) = obj(field("message", string(Option(error.getMessage).getOrElse(error.getClass.getName))))
}

class ResponseBuilder private() {

  private val response = Response()

  def withCode(code: HttpResponseStatus): ResponseBuilder = {
    response.setStatusCode(code.getCode)
    this
  }

  def withContent(jsonContent: JsonRootNode): ResponseBuilder = {
    response.setContentTypeJson()
    withContent(formatter.format(jsonContent))
  }

  def withContent(content: String): ResponseBuilder = {
    response.setContent(copiedBuffer(content, UTF_8))
    this
  }

  def toFuture: Future[Response] = Future.value(response)

  def build = response
}