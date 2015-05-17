package util

import argo.jdom.JsonNodeFactories._
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FinagleTypeAliases.{FTService, FTResponse, FTRequest}
import io.github.daviddenton.fintrospect.util.ArgoUtil.obj
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import ResponseBuilder._

case class Echo(parts: String*) extends FTService {
  def apply(request: FTRequest): Future[FTResponse] = {
    Ok(obj(
      "headers" -> string(request.headers().toString()),
      "params" -> string(request.toString), // ERROR!
      "message" -> string(parts.mkString(" "))))
  }
}
