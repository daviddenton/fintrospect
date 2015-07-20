package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

abstract class QueryParameter[T](val name: String, val description: Option[String], val paramType:ParamType)
  extends Parameter
  with Validatable[T, HttpRequest]
  with Bindable[T, QueryBinding] {

  val where = "query"
}

abstract class SingleQueryParameter[T](spec: ParameterSpec[T]) extends QueryParameter[T](spec.name, spec.description, spec.paramType) {

  override def -->(value: T) = Seq(new QueryBinding(this, spec.serialize(value)))

  def validate(request: HttpRequest) =
    Try(new QueryStringDecoder(request.getUri).getParameters.get(name).asScala.toSeq.head).toOption.map {
      v => Try(spec.deserialize(v)) match {
        case Success(d) => Right(Option(d))
        case Failure(_) => Left(this)
      }
    }.getOrElse(if (required) Left(this) else Right(None))
}

abstract class MultiQueryParameter[T](spec: ParameterSpec[T]) extends QueryParameter[Seq[T]](spec.name, spec.description, spec.paramType) {

  override def -->(value: Seq[T]) = value.map(v => new QueryBinding(this, spec.serialize(v)))

  def validate(request: HttpRequest) =
    Try(new QueryStringDecoder(request.getUri).getParameters.get(name).asScala.toSeq).toOption.map {
      v =>
        Try(v.map(s => spec.deserialize(s))) match {
          case Success(d) => Right(Option(d))
          case Failure(_) => Left(this)
        }
    }.getOrElse(if (required) Left(this) else Right(None))
}
