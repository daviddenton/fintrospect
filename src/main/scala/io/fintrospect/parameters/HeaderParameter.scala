package io.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.util.{Failure, Success, Try}

abstract class HeaderParameter[T](spec: ParameterSpec[T])
  extends Parameter
  with Validatable[T, Request]
  with Bindable[T, RequestBinding] {
  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  override def -->(value: T) = Seq(new RequestBinding(this, in => {
    in.headerMap.add(name, spec.serialize(value))
    in
  }))

  val where = "header"

  def validate(request: Request) = {
    request.headerMap.get(name).map {
      v => Try(spec.deserialize(v)) match {
        case Success(d) => Right(Option(d))
        case Failure(_) => Left(this)
      }
    }.getOrElse(if (required) Left(this) else Right(None))
  }
}
