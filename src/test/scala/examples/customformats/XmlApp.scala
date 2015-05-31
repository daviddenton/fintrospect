package examples.customformats

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import io.github.daviddenton.fintrospect.util.ResponseBuilder.toFuture
import io.github.daviddenton.fintrospect.{CorsFilter, DescribedRoute, FintrospectModule}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

/**
 * This application shows how to define a custom rendering format.
 */
object XmlApp extends App {
  private def aService(): Service[HttpRequest, HttpResponse] = Service.mk((rq) => toFuture(XmlResponseBuilder.Ok))

  val module = FintrospectModule(Root / "xml", Xml)
    .withRoute(DescribedRoute("an xml endpoint").at(HttpMethod.GET) / "view" bindTo aService)

  Http.serve(":8080", new CorsFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))

  println("See the service description at: http://localhost:8080/xml")

  Thread.currentThread().join()
}
