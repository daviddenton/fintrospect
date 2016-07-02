package io.fintrospect.templating

import com.github.mustachejava.Mustache
import io.fintrospect.formats.Html

class RenderMustacheViewTest extends RenderTemplateEngineSpec[Mustache](MustacheTemplateLoaders, "mustache") {
  override def renderViewFor(loader: TemplateLoader[Mustache]) = new RenderMustacheView(Html.ResponseBuilder, loader)
}
