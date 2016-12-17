package examples.customformats

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import examples.customformats.HipsterXml.ResponseBuilder.implicits.{responseBuilderToFuture, statusToResponseBuilderConfig}
import io.fintrospect.parameters._
import io.fintrospect.{ContentTypes, RouteModule, RouteSpec}

import scala.language.reflectiveCalls

/**
  * This application shows how to define a custom rendering, body and parameter formats.
  */
object HipsterXmlApp extends App {

  def aService(hipsterBeardStyle: HipsterBeardStyle): Service[Request, Response] = Service.mk((rq) => Ok(hipsterBeardStyle.name))

  val xmlAsABody = BodySpec.string(Option("An XML document"), ContentTypes.APPLICATION_XML).map(s => HipsterXmlFormat(s), (x: HipsterXmlFormat) => x.value)
  val xmlAsAParam = ParameterSpec.string("anXmlParameter", "An XML document").map(i => HipsterXmlFormat(i), (e: HipsterXmlFormat) => e.value)

  val route = RouteSpec("an xml endpoint")
    .taking(Header.optional(xmlAsAParam))
    .body(Body(xmlAsABody))
    .at(Get) / "view" / Path(HipsterBeardStyle) bindTo aService

  val module = RouteModule(Root / "xml", HipsterXmlModuleRenderer).withRoute(route)

  println("See the service description at: http://localhost:8080/xml")

  Await.ready(
    Http.serve(":8080", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))
  )
}
