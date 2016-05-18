package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import io.fintrospect.parameters.InvalidParameter.Invalid
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}

/**
  * Represents a single entity which makes up the entirety of an HTTP message body.
  * @param spec the specification of this body type
  * @param theParamType the documented type of this body. Usually this is StringParamType, apart from for JSON, which is ObjectParamType
  * @param theExample an example object of this body
  * @tparam T the type of the request when it has been deserialized from the request
  */
class UniBody[T](spec: BodySpec[T],
                 theParamType: ParamType,
                 theExample: Option[T])
  extends Body(spec)
  with Bindable[T, RequestBinding]
  with MandatoryRebind[T, Message, RequestBinding] {

  private val param = new BodyParameter with Bindable[T, RequestBinding] {
    override val required = true
    override val description = spec.description
    override val name = "body"
    override val where = "body"
    override val paramType = theParamType

    override def -->(value: T) = Seq(new RequestBinding(this, t => {
      val content = spec.serialize(value)
      t.headerMap.add(Names.CONTENT_TYPE, spec.contentType.value)
      t.headerMap.add(Names.CONTENT_LENGTH, content.length.toString)
      t.setContentString(content)
      t
    }))

    override val example = theExample.map(spec.serialize)
  }

  override def -->(value: T) = param.of(value)

  override def iterator = Iterator(param)

  override protected def extract(message: Message): Extraction[T] =
    Try(spec.deserialize(contentFrom(message))) match {
      case Success(v) => Extracted(Some(v))
      case Failure(_) => ExtractionFailed(Invalid(param))
    }
}
