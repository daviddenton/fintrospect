package examples.customformats

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import io.fintrospect._
import io.fintrospect.parameters._
import io.fintrospect.util.ResponseBuilder.toFuture
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

/**
 * This application shows how to define a custom rendering, body and parameter formats.
 */
object XmlApp extends App {

  def aService(): Service[HttpRequest, HttpResponse] = Service.mk((rq) => toFuture(XmlResponseBuilder.Ok))

  val xmlAsABody = BodySpec[XmlFormat](Some("An XML document"), ContentTypes.APPLICATION_XML, XmlFormat(_), _.value)
  val xmlAsAParam = ParameterSpec[XmlFormat]("anXmlParameter", Some("An XML document"), StringParamType, XmlFormat(_), _.value)

  val route = DescribedRoute("an xml endpoint")
    .taking(Header.optional(xmlAsAParam))
    .body(Body(xmlAsABody))
    .at(HttpMethod.GET) / "view" bindTo aService

  val module = FintrospectModule(Root / "xml", Xml).withRoute(route)

  Http.serve(":8080", new CorsFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))

  println("See the service description at: http://localhost:8080/xml")

  Thread.currentThread().join()
}
