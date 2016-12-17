package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import com.twitter.io.Buf.ByteArray.Shared
import io.fintrospect.util.ExtractionError.Invalid
import io.fintrospect.util.{Extracted, Extraction, ExtractionFailed}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}

/**
  * Represents a single entity which makes up the entirety of an HTTP message body.
  *
  * @param spec         the specification of this body type
  * @param theExample   an example object of this body
  * @tparam T the type of the request when it has been deserialized from the request
  */
class UniBody[T](spec: BodySpec[T],
                 theExample: Option[T])
  extends Body[T] {
  private val param = new BodyParameter with Bindable[T, RequestBinding] {
    override val required = true
    override val description = spec.description
    override val name = "body"
    override val where = "body"
    override val paramType = spec.paramType

    override def -->(value: T) = Seq(new RequestBinding(this, t => {
      val content = spec.serialize(value)
      t.headerMap.add(Names.CONTENT_TYPE, spec.contentType.value)
      t.headerMap.add(Names.CONTENT_LENGTH, content.length.toString)
      t.content = content
      t
    }))

    override val example = theExample.map(spec.serialize).map(b => new String(Shared.extract(b)))
  }

  override val contentType = spec.contentType

  override def -->(value: T) = param.of(value)

  override def iterator = Iterator(param)

  override def <--?(message: Message): Extraction[T] = message match {
    case req: ExtractedRouteRequest => req.get(this)
    case _ => Try(spec.deserialize(message.content)) match {
      case Success(v) => Extracted(Some(v))
      case Failure(_) => ExtractionFailed(Invalid(param))
    }
  }
}
