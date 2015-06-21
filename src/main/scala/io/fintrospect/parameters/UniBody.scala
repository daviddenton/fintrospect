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
                 theExample: Option[JsonRootNode]) extends Body[T] {

  private val param = new BodyParameter[T] {
    override val required = true
    override val description = spec.description
    override val name = "body"
    override val where = "body"
    override val paramType = theParamType

    override def ->(value: T): ParamBinding[T] = ???

    override val example: Option[JsonRootNode] = theExample
  }

  override def from(request: HttpRequest): T = {
    val validate1: List[Either[Parameter[T], Option[T]]] = validate(request)
    validate1.head.right.get.get
  }

  override val contentType = spec.contentType

  override def iterator = Iterator(param)

  override def validate(request: HttpRequest): List[Either[Parameter[T], Option[T]]] = {
    val from = Try(contentFrom(request)).toOption
    List(
      if (from.isEmpty) {
        Left(param)
      } else {
        Try(spec.deserialize(from.get)) match {
          case Success(v) => Right(Some(v))
          case Failure(_) => Left(param)
        }
      }
    )
  }
}
