package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.util.ArgoUtil
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import io.fintrospect.{ContentType, ContentTypes}
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

trait Body extends Iterable[Parameter[_]] {
  val contentType: ContentType
}

object Body {
  private val location = new Location {
    override def toString = "body"

    override def from(unused: String, request: HttpRequest): Option[String] = Try(contentFrom(request)).toOption

    override def into(name: String, value: String, request: HttpRequest): Unit = ???
  }

  object JsonBody extends Body {
    override val contentType: ContentType = ContentTypes.APPLICATION_JSON

    override def iterator: Iterator[Parameter[_]] = Nil.iterator
  }


  /**
   * Defines the JSON body of a request.
   * @param description
   * @param example
   */
  def json(description: Option[String], example: JsonRootNode) =
    new BodyParameter[JsonRootNode]("body", description, ObjectParamType, example, location, ArgoUtil.parse, ArgoUtil.compact) with Mandatory[JsonRootNode]
}
