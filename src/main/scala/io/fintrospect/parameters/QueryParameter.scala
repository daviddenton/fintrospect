package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import io.fintrospect.parameters.InvalidParameter.Invalid
import org.jboss.netty.handler.codec.http.QueryStringDecoder

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

abstract class QueryParameter[T](spec: ParameterSpec[_], val deserialize: Seq[String] => T)
  extends Parameter with Bindable[T, QueryBinding] {

  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType
  override val where = "query"

  protected def get[O](request: Request, fn: T => O, default: Extraction[O]): Extraction[O] =
    Option(new QueryStringDecoder(request.uri).getParameters.get(name))
      .map(_.asScala.toSeq)
      .map(v =>
        Try(deserialize(v)) match {
          case Success(d) => Extracted(fn(d))
          case Failure(_) => ExtractionFailed(Invalid(this))
        }).getOrElse(default)
}

abstract class SingleQueryParameter[T](spec: ParameterSpec[T])
  extends QueryParameter[T](spec, xs => spec.deserialize(xs.head)) {
  override def -->(value: T) = Seq(new QueryBinding(this, spec.serialize(value)))
}

abstract class MultiQueryParameter[T](spec: ParameterSpec[T])
  extends QueryParameter[Seq[T]](spec, xs => xs.map(spec.deserialize)) {
  override def -->(value: Seq[T]) = value.map(v => new QueryBinding(this, spec.serialize(v)))
}
