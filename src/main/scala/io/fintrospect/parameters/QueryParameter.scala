package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import org.jboss.netty.handler.codec.http.QueryStringDecoder

import scala.collection.JavaConverters._

abstract class QueryParameter[T](spec: ParameterSpec[_])
  extends Parameter with Bindable[T, QueryBinding] {

  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType
  override val where = "query"

  protected def extract(request: Request): Option[Seq[String]] =
    Option(new QueryStringDecoder(request.uri).getParameters.get(name)).map(_.asScala.toSeq)
}

abstract class SingleQueryParameter[T](spec: ParameterSpec[T])
  extends QueryParameter[T](spec) {
  override def -->(value: T) = Seq(new QueryBinding(this, spec.serialize(value)))

  def get(request: Request) = Extraction.extract(this, xs => spec.deserialize(xs.head), extract(request))
}

abstract class MultiQueryParameter[T](spec: ParameterSpec[T])
  extends QueryParameter[Seq[T]](spec) {
  override def -->(value: Seq[T]) = value.map(v => new QueryBinding(this, spec.serialize(v)))

  def get(request: Request) = Extraction.extract(this, xs => xs.map(spec.deserialize), extract(request))
}
