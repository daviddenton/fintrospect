package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

abstract class QueryParameter[BASE, WRAPPED](protected val spec: ParameterSpec[BASE])
  extends Parameter
  with Validatable[WRAPPED, HttpRequest]
  with Bindable[WRAPPED, QueryBinding] {
  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  val where = "query"
}

abstract class SingleQueryParameter[T](spec: ParameterSpec[T]) extends QueryParameter[T, T](spec) {

  override def -->(value: T) = Seq(new QueryBinding(this, spec.serialize(value)))

  def validate(request: HttpRequest) =
    Try(new QueryStringDecoder(request.getUri).getParameters.get(name).get(0)).toOption.map {
      v => Try(spec.deserialize(v)) match {
        case Success(d) => Right(Option(d))
        case Failure(_) => Left(this)
      }
    }.getOrElse(if (required) Left(this) else Right(None))
}

abstract class MultiQueryParameter[T](spec: ParameterSpec[T]) extends QueryParameter[T, Seq[T]](spec) {

  override def -->(value: Seq[T]) = value.map(v => new QueryBinding(this, spec.serialize(v)))

  def validate(request: HttpRequest) =
    Try(new QueryStringDecoder(request.getUri).getParameters.get(name).asScala.toSeq).toOption
      .map {
      v =>
        Try(v.map(s => spec.deserialize(s))) match {
          case Success(d) => Right(Option(d))
          case Failure(_) => Left(this)
        }
    }.getOrElse(if (required) Left(this) else Right(None))
}
