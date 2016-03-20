package examples.customformats

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import examples.customformats.HipsterXml.ResponseBuilder.statusToResponseBuilderConfig
import io.fintrospect.parameters.{Path, Body, Header, StringParamType, ParameterSpec, BodySpec}
import io.fintrospect.{ContentTypes, ModuleSpec, RouteSpec}

/**
 * This application shows how to define a custom rendering, body and parameter formats.
 */
object HipsterXmlApp extends App {

  def aService(hipsterBeardStyle: HipsterBeardStyle): Service[Request, Response] = Service.mk((rq) => Ok(hipsterBeardStyle.name))

  val xmlAsABody = BodySpec[HipsterXmlFormat](Option("An XML document"), ContentTypes.APPLICATION_XML, HipsterXmlFormat(_), _.value)
  val xmlAsAParam = ParameterSpec[HipsterXmlFormat]("anXmlParameter", Option("An XML document"), StringParamType, HipsterXmlFormat(_), _.value)

  val route = RouteSpec("an xml endpoint")
    .taking(Header.optional(xmlAsAParam))
    .body(Body(xmlAsABody))
    .at(Get) / "view" / Path(HipsterBeardStyle) bindTo aService

  val module = ModuleSpec(Root / "xml", HipsterXmlModuleRenderer).withRoute(route)

  Http.serve(":8080", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))

  println("See the service description at: http://localhost:8080/xml")

  Thread.currentThread().join()
}
