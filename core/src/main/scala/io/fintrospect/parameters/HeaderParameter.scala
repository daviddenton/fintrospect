package io.fintrospect.parameters

import com.twitter.finagle.http.{Message, Request}

trait HeaderParameter[T]
  extends Parameter with Rebindable[Message, T, RequestBinding] {

  override val where = "header"
}

object HeaderExtractAndRebind extends ParameterExtractAndBind[Message, RequestBinding] {
  def newBinding(parameter: Parameter, value: String) = new RequestBinding(parameter, {
    req: Request => {
      req.headerMap.add(parameter.name, value)
      req
    }
  })

  def valuesFrom(parameter: Parameter, message: Message): Option[Seq[String]] = {
    val headers = message.headerMap.getAll(parameter.name)
    if (headers.isEmpty) None else Some(headers.toSeq)
  }
}

abstract class MultiHeaderParameter[T](spec: ParameterSpec[T])
  extends MultiParameter(spec, HeaderExtractAndRebind) with HeaderParameter[Seq[T]] {
}

