package io.fintrospect.parameters

import com.twitter.finagle.http.{Message, Request}

trait HeaderParameter[T]
  extends Parameter
  with Bindable[T, RequestBinding] {

  val where = "header"

  protected def valuesFrom(message: Message): Option[Seq[String]] = {
    val headers = message.headerMap.getAll(name)
    if (headers.isEmpty) None else Some(headers.toSeq)
  }
}

abstract class SingleHeaderParameter[T](spec: ParameterSpec[T])
  extends SingleParameter[T, Message, RequestBinding](spec,
    (p, s) => new RequestBinding(p, {
      req: Request => {
        req.headerMap.add(spec.name, s)
        req
      }
    })) with HeaderParameter[T] {
}

abstract class MultiHeaderParameter[T](spec: ParameterSpec[T])
  extends MultiParameter[T, Message, RequestBinding](spec,
    (p, s) => new RequestBinding(p, {
      req: Request => {
        req.headerMap.add(spec.name, s)
        req
      }
    })) with HeaderParameter[Seq[T]] {
}

