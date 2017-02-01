package examples.customformats

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import examples.customformats.HipsterXml.ResponseBuilder._
import io.fintrospect.parameters._
import io.fintrospect.{ContentTypes, RouteModule, RouteSpec}

import scala.language.reflectiveCalls

/**
  * This application shows how to define a custom rendering, body and parameter formats.
  */
object HipsterXmlApp extends App {

  def aService(hipsterBeardStyle: HipsterBeardStyle): Service[Request, Response] = Service.mk((rq) => Ok(hipsterBeardStyle.value))

  val xmlAsABody = BodySpec.string(ContentTypes.APPLICATION_XML).map(s => HipsterXmlFormat(s), (x: HipsterXmlFormat) => x.value)
  val xmlAsAParam = ParameterSpec.string().map(i => HipsterXmlFormat(i), (e: HipsterXmlFormat) => e.value)
  val xmlAsPath = Path(ParameterSpec.string().as[HipsterBeardStyle], "anXmlParameter", "An XML document")

  val route = RouteSpec("an xml endpoint")
    .taking(Header.optional(xmlAsAParam, "An XML document"))
    .body(Body(xmlAsABody))
    .at(Get) / "view" / xmlAsPath bindTo aService

  val module = RouteModule(Root / "xml", HipsterXmlModuleRenderer).withRoute(route)

  println("See the service description at: http://localhost:8080/xml")

  Await.ready(
    Http.serve(":8080", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))
  )
}
