package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import com.twitter.io.Buf.ByteArray.Shared
import io.fintrospect.util.ExtractionError.Invalid
import io.fintrospect.util.{Extracted, Extraction, ExtractionFailed}

import scala.util.{Failure, Success, Try}

/**
  * Represents a single entity which makes up the entirety of an HTTP message body.
  *
  * @param spec       the specification of this body type
  * @param theExample an example object of this body
  * @tparam T the type of the request when it has been deserialized from the request
  */
case class UniBody[T](inDescription: String, spec: BodySpec[T],
                      theExample: Option[T])
  extends Body[T] {
  private val param = new BodyParameter with Bindable[T, RequestBinding] {
    override val required = true
    override val description = inDescription
    override val name = "body"
    override val where = "body"
    override val paramType = spec.paramType

    override def -->(value: T) = Seq(new RequestBinding(this, t => {
      val content = spec.serialize(value)
      t.headerMap.add("Content-type", spec.contentType.value)
      t.headerMap.add("Content-length", content.length.toString)
      t.content = content
      t
    }))

    override def iterator: Iterator[Parameter] = Seq(this).iterator

    override val example = theExample.map(spec.serialize).map(b => new String(Shared.extract(b)))
  }

  override val contentType = spec.contentType

  override def -->(value: T) = param.of(value)

  override def iterator = Iterator(param)

  private val fallback: Message => Extraction[T] = message => Try(spec.deserialize(message.content)) match {
    case Success(v) => Extracted(v)
    case Failure(_) => ExtractionFailed(Invalid(param))
  }

  override def <--?(message: Message): Extraction[T] = message match {
    case req: ExtractedRouteRequest => req.get(this, fallback)
    case _ => fallback(message)
  }
}
