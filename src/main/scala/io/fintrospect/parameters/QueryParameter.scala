package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import org.jboss.netty.handler.codec.http.QueryStringDecoder

import scala.collection.JavaConverters._

trait QueryParameter[T]
  extends Parameter with Bindable[T, QueryBinding] {

  val spec: ParameterSpec[_]
  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType
  override val where = "query"

  protected def valuesFrom(request: Request): Option[Seq[String]] =
    Option(new QueryStringDecoder(request.uri).getParameters.get(name)).map(_.asScala.toSeq)
}

abstract class SingleQueryParameter[T](spec: ParameterSpec[T])
  extends SingleParameter[T, Request, QueryBinding](spec, new QueryBinding(_, _)) with QueryParameter[T] {
}

abstract class MultiQueryParameter[T](spec: ParameterSpec[T])
  extends MultiParameter[T, Request, QueryBinding](spec, new QueryBinding(_, _)) with QueryParameter[Seq[T]] {
}
