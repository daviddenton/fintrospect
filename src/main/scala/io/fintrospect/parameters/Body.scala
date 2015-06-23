package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import com.twitter.io.Charsets
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes._
import io.fintrospect.util.ArgoUtil
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.jboss.netty.handler.codec.http.HttpRequest

abstract class Body[T](spec: BodySpec[T]) extends Iterable[BodyParameter[_]] with Retrieval[T, HttpRequest] with Bindable[T] {
  val contentType: ContentType

  def validate(request: HttpRequest): Seq[Either[Parameter[_], Option[_]]]

  override def ->(value: T): Binding = RequestBinding(t => {
    val content = copiedBuffer(spec.serialize(value), Charsets.Utf8)
    t.headers().add(Names.CONTENT_TYPE, contentType.value)
    t.headers().add(Names.CONTENT_LENGTH, String.valueOf(content.readableBytes()))
    t.setContent(content)
    t
  })
}

object Body {

  /**
   * Create a custom body type for the request. Encapsulates the means to insert/retrieve into the request
   */
  def apply[T](bodySpec: BodySpec[T]): Body[T] = new UniBody[T](bodySpec, StringParamType, None)

  def json(description: Option[String], example: JsonRootNode = null): Body[JsonRootNode] =
    new UniBody[JsonRootNode](BodySpec(description, APPLICATION_JSON, ArgoUtil.parse, ArgoUtil.compact), ObjectParamType, Option(example))

  def form(fields: FormField[_] with Retrieval[_, Form]*): Body[Form] = new FormBody(fields)
}
