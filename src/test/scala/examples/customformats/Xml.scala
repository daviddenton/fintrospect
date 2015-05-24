package examples.customformats

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.renderers.ModuleRenderer
import io.github.daviddenton.fintrospect.{DescriptionRenderer, Route}

class Xml private() extends DescriptionRenderer[XmlFormat] {
  private def renderRoute(basePath: Path, route: Route): XmlFormat = XmlFormat( s"<entry>${route.method}:${route.describeFor(basePath)}</entry>")

  private def renderRoutes(basePath: Path, routes: Seq[Route]): String = XmlFormat(routes.map(renderRoute(basePath, _)): _*).toString()

  def apply(basePath: Path, routes: Seq[Route]): XmlFormat = XmlFormat( s"<paths>${renderRoutes(basePath, routes)}</paths>")
}

/**
 * Hyper-cool, next-gen, markup used by all true rockstar coderzzzz
 */
object Xml {
  def apply(): ModuleRenderer[XmlFormat] = new ModuleRenderer[XmlFormat](
    XmlResponseBuilder.Response,
    new Xml(),
    (bp) => XmlFormat(bp.toString())
  )
}
