package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import org.jboss.netty.handler.codec.http.QueryStringDecoder

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

abstract class QueryParameter[T](spec: ParameterSpec[_], val deserialize: Seq[String] => T)
  extends Parameter with Validatable[T, Request] with Bindable[T, QueryBinding] {

  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType
  override val where = "query"

  def validate(request: Request) = {
    Option(new QueryStringDecoder(request.uri).getParameters.get(name))
      .map(_.asScala.toSeq)
      .map(v =>
      Try(deserialize(v)) match {
        case Success(d) => Extracted(d)
        case Failure(_) => MissingOrInvalid[T](this)
      }).getOrElse(if (required) MissingOrInvalid(this) else Missing())
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
