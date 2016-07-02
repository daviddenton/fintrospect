package io.fintrospect.templating

import com.gilt.handlebars.scala.Handlebars
import io.fintrospect.formats.Html

class RenderHandlebarsViewTest extends RenderTemplateEngineSpec[Handlebars[Any]](HandlebarsTemplateLoaders, "handlebars") {
  override def renderViewFor(loader: TemplateLoader[Handlebars[Any]]) = new RenderHandlebarsView(Html.ResponseBuilder, loader)
}
