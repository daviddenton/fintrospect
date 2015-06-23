package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
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
                 theExample: Option[JsonRootNode]) extends Body[T](spec) {

  private val param = new BodyParameter[T] {
    override val required = true
    override val description = spec.description
    override val name = "body"
    override val where = "body"
    override val paramType = theParamType

    override def ->(value: T) = ???

    override val example = theExample
  }

  override def from(request: HttpRequest) = validate(request).head.right.get.get

  override val contentType = spec.contentType

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
