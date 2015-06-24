package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.util.{Failure, Success, Try}

abstract class QueryParameter[T](spec: ParameterSpec[T]) extends Parameter[T] with Validatable[T, HttpRequest] {
  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  def into(request: HttpRequest, value: String): Unit = ???

  private def from(name: String, request: HttpRequest) = {
    Try(new QueryStringDecoder(request.getUri).getParameters.get(name)).map(_.get(0)).toOption
  }

  override def ->(value: T) = Bindings(QueryBinding(this, name -> spec.serialize(value)))

  val where = "query"

  def validate(request: HttpRequest) = {
    from(name, request).map {
      v => Try(spec.deserialize(v)) match {
        case Success(d) => Right(Option(d))
        case Failure(_) => Left(this)
      }
    }.getOrElse(if (required) Left(this) else Right(None))
  }

}
