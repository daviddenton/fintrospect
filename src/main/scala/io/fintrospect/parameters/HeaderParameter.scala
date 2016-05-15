package io.fintrospect.parameters

import com.twitter.finagle.http.{Message, Request}

abstract class HeaderParameter[T](spec: ParameterSpec[_])
  extends Parameter
  with Bindable[T, RequestBinding] {
  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType
  val where = "header"

  protected def extract(message: Message): Option[Seq[String]] = {
    val headers = message.headerMap.getAll(name)
    if (headers.isEmpty) None else Some(headers.toSeq)
  }
}

abstract class SingleHeaderParameter[T](spec: ParameterSpec[T])
  extends HeaderParameter[T](spec) {

  protected def get(message: Message) = Extraction(this, xs => spec.deserialize(xs.head), extract(message))

  override def -->(value: T) = Seq(new RequestBinding(this, {
    req: Request => {
      req.headerMap.add(spec.name, spec.serialize(value))
      req
    }
  }))
}

abstract class MultiHeaderParameter[T](spec: ParameterSpec[T])
  extends HeaderParameter[Seq[T]](spec) {

  protected def get(message: Message) = Extraction(this, xs => xs.map(spec.deserialize), extract(message))

  override def -->(value: Seq[T]) = value.map(v => new RequestBinding(this, {
    req: Request => {
      req.headerMap.add(spec.name, spec.serialize(v))
      req
    }
  }))
}

