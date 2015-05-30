package examples.customformats

import examples.customformats.XmlResponseBuilder._
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Route
import io.github.daviddenton.fintrospect.renderers.DescriptionRenderer
import org.jboss.netty.handler.codec.http.HttpResponse

class XmlDescriptionRenderer extends DescriptionRenderer {
  private def renderRoute(basePath: Path, route: Route): XmlFormat = XmlFormat( s"<entry>${route.method}:${route.describeFor(basePath)}</entry>")

  private def renderRoutes(basePath: Path, routes: Seq[Route]): String = XmlFormat(routes.map(renderRoute(basePath, _)): _*).toString()

  def apply(basePath: Path, routes: Seq[Route]): HttpResponse = Ok(XmlFormat( s"<paths>${renderRoutes(basePath, routes)}</paths>").value)
}
