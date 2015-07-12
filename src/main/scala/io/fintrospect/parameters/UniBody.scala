package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import com.twitter.io.Charsets
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.{Failure, Success, Try}

/**
 * Represents a generic body which can be written to and retrieved from a request.
 * @param spec the specification of this body type
 * @param theParamType the documented type of this body. Usually this is StringParamType, apart from for JSON, which is ObjectParamType
 * @param theExample (JSON types only) an example object of this body
 * @tparam T the type of the request when it has been deserialized from the request
 */
class UniBody[T](spec: BodySpec[T],
                 theParamType: ParamType,
                 theExample: Option[JsonRootNode])
  extends Body[T](spec)
  with Bindable[T, RequestBinding]
  with MandatoryRebind[T, HttpRequest, RequestBinding] {

  private val param = new BodyParameter[T] with Bindable[T, RequestBinding] {
    override val required = true
    override val description = spec.description
    override val name = "body"
    override val where = "body"
    override val paramType = theParamType

    override def -->(value: T) = Seq(new RequestBinding(this, t => {
      val content = copiedBuffer(spec.serialize(value), Charsets.Utf8)
      t.headers().add(Names.CONTENT_TYPE, spec.contentType.value)
      t.headers().add(Names.CONTENT_LENGTH, String.valueOf(content.readableBytes()))
      t.setContent(content)
      t
    }))

    override val example = theExample
  }

  override def -->(value: T) = param.of(value)

  override def <--(request: HttpRequest) = validate(request).head.right.get.get

  override def iterator = Iterator(param)

  override def validate(request: HttpRequest): Seq[Either[Parameter[T], Option[T]]] = {
    val from = Try(contentFrom(request)).toOption
    Seq(
      if (from.isEmpty) {
        Left(param)
      } else {
        Try(spec.deserialize(from.get)) match {
          case Success(v) => Right(Option(v))
          case Failure(_) => Left(param)
        }
      }
    )
  }

}
