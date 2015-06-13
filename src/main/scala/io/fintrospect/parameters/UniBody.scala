package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.ContentType
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

class UniBody[T](val contentType: ContentType,
                 theDescription: Option[String],
                 theParamType: ParamType,
                 val example: Option[JsonRootNode],
                 deserialize: String => T,
                 serialize: T => String) extends Body[T] {

  private val location = new Location {

    override def toString = "body"
    override def from(unused: String, request: HttpRequest): Option[String] = Try(contentFrom(request)).toOption

    override def into(name: String, value: String, request: HttpRequest): Unit = ???
  }

  private val param = new BodyParameter[T]("body", theDescription, theParamType, example, location, deserialize, serialize) with Mandatory[T]

  override def parameterParts: Seq[BodyParameter[_]] = Seq(param)

  override def from(request: HttpRequest): T = deserialize(location.from(null, request).get)
}
