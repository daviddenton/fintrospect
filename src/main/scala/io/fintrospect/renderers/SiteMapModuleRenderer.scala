package io.fintrospect.renderers

import java.net.URL

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Response, Status}
import io.fintrospect.ServerRoute
import io.fintrospect.formats.Xml.ResponseBuilder._
import io.fintrospect.parameters.{Parameter, Security}

class SiteMapModuleRenderer(baseUrl: URL) extends ModuleRenderer {

  override def badRequest(badParameters: Seq[Parameter]): Response = Error(Status.BadRequest, badParameters.toString())

  override def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_]]): Response = {
    def buildUrl(route: ServerRoute[_]) = {
      <url>
        <loc>
          {baseUrl + route.describeFor(basePath)}
        </loc>
      </url>
    }

    Ok(<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      {routes.filter(_.method == Get).map(buildUrl)}
    </urlset>)
  }
}
