package examples.customformats

import com.twitter.finagle.httpx.Method._
import com.twitter.finagle.httpx.filter.Cors
import com.twitter.finagle.httpx.path.Root
import com.twitter.finagle.httpx.{Request, Response}
import com.twitter.finagle.{Httpx, Service}
import examples.customformats.HipsterXmlResponseBuilder._
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder.toFuture
import io.fintrospect.parameters._

/**
 * This application shows how to define a custom rendering, body and parameter formats.
 */
object HipsterXmlApp extends App {

  def aService(): Service[Request, Response] = Service.mk((rq) => OK)

  val xmlAsABody = BodySpec[HipsterXmlFormat](Option("An XML document"), ContentTypes.APPLICATION_XML, HipsterXmlFormat(_), _.value)
  val xmlAsAParam = ParameterSpec[HipsterXmlFormat]("anXmlParameter", Option("An XML document"), StringParamType, HipsterXmlFormat(_), _.value)

  val route = RouteSpec("an xml endpoint")
    .taking(Header.optional(xmlAsAParam))
    .body(Body(xmlAsABody))
    .at(Get) / "view" bindTo aService

  val module = FintrospectModule(Root / "xml", HipsterXml).withRoute(route)

  Httpx.serve(":8080", new CorsFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))

  println("See the service description at: http://localhost:8080/xml")

  Thread.currentThread().join()
}
