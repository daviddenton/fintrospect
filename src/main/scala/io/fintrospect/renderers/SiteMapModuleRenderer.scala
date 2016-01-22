package io.fintrospect.renderers

import java.net.URL

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Response, Status}
import io.fintrospect.ServerRoute
import io.fintrospect.formats.Xml.ResponseBuilder._
import io.fintrospect.parameters.{Parameter, Security}

class SiteMapModuleRenderer(baseUrl: URL) extends ModuleRenderer {

  override def badRequest(badParameters: Seq[Parameter]): Response = Error(Status.BadRequest, badParameters.toString())

  override def description(basePath: Path, security: Security, routes: Seq[ServerRoute]): Response = {
    def buildUrl(route: ServerRoute) = {
      <url>
        <loc>
          {baseUrl + route.describeFor(basePath)}
        </loc>
      </url>
    }

    Ok(<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      {routes.map(buildUrl)}
    </urlset>)
  }
}
