package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

abstract class QueryParameter[T](spec: ParameterSpec[_], val deserialize: Seq[String] => T)
  extends Parameter with Validatable[T, HttpRequest] with Bindable[T, QueryBinding] {

  val name = spec.name
  val description = spec.description
  val paramType = spec.paramType
  val where = "query"

  def validate(request: HttpRequest) = {
    Option(new QueryStringDecoder(request.getUri).getParameters.get(name))
      .map(_.asScala.toSeq)
      .map(v =>
      Try(deserialize(v)) match {
        case Success(d) => Right(Option(d))
        case Failure(_) => Left(this)
      }).getOrElse(if (required) Left(this) else Right(None))
  }
}

abstract class SingleQueryParameter[T](spec: ParameterSpec[T])
  extends QueryParameter[T](spec, xs => spec.deserialize(xs.head)) {
  override def -->(value: T) = Seq(new QueryBinding(this, spec.serialize(value)))
}

abstract class MultiQueryParameter[T](spec: ParameterSpec[T])
  extends QueryParameter[Seq[T]](spec, xs => xs.map(spec.deserialize)) {
  override def -->(value: Seq[T]) = value.map(v => new QueryBinding(this, spec.serialize(v)))
}
