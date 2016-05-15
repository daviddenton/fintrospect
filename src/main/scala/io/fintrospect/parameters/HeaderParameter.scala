package io.fintrospect.parameters

import com.twitter.finagle.http.{Message, Request}

abstract class HeaderParameter[T](spec: ParameterSpec[_], val deserialize: Seq[String] => T)
  extends Parameter
  with Deserialisable[T]
  with Bindable[T, RequestBinding] {
  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType
  val where = "header"

  protected def get[O](message: Message, fn: T => O): Extraction[O] = {
    val headers = message.headerMap.getAll(name)
    Extractor.extract(this, if (headers.isEmpty) None else Some(headers.toSeq)).map(fn)
  }
}

abstract class SingleHeaderParameter[T](spec: ParameterSpec[T])
  extends HeaderParameter[T](spec, xs => spec.deserialize(xs.head)) {
  override def -->(value: T) = Seq(new RequestBinding(this, {
    req: Request => {
      req.headerMap.add(spec.name, spec.serialize(value))
      req
    }
  }))
}

abstract class MultiHeaderParameter[T](spec: ParameterSpec[T])
  extends HeaderParameter[Seq[T]](spec, xs => xs.map(spec.deserialize)) {
  override def -->(value: Seq[T]) = value.map(v => new RequestBinding(this, {
    req: Request => {
      req.headerMap.add(spec.name, spec.serialize(v))
      req
    }
  }))
}

