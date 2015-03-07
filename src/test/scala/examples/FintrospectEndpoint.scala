package examples

import io.github.daviddenton.fintrospect._
import org.jboss.netty.handler.codec.http.HttpMethod

class FintrospectEndpoint {
  val description = Description("a get endpoint", HttpMethod.GET, _ / "echo")

//  val queryParam = Parameter.path("term", classOf[String])
  def addTo(module: FintrospectModule): FintrospectModule = {
    module.withRoute(description, SegmentMatchers.string("message"), null)
    null
  }
}

object FintrospectEndpoint extends App {
  println(Parameters.query[Int]("bob").paramType)
}
