##Templating
Templates are applied by using custom Filters to convert View instances into standard Http Responses. Simply implement the View trait and then put a matching template file onto the classpath, and chain the output of the model-creating Service into the Filter. You can do this for entire modules by making the ModuleSpec itself generified on View and using the templating Filter as a Module-level filter:
```
case class ViewMessage(value: String) extends View

def showMessage() = Service.mk[Request, View] { _ => Future.value(ViewMessage("some value to be displayed")) }

val webModule = ModuleSpec[View](Root / "web",
    new SiteMapModuleRenderer(new URL("http://root.com")),
    new RenderMustacheView(Html.ResponseBuilder))
    .withRoute(RouteSpec().at(Get) / "message" bindTo showMessage)
```
Similarly to how the ResponseBuilders work, no 3rd-party dependencies are bundled with Fintrospect - simply import the extra SBT dependencies as required:
<table border="1px">
<tr>
  <td>Library</td>
  <td>Template filename suffix</td>
  <td>Additional SBT depdendencies</td>
  <td>Filter class</td>
</tr>
<tr>
  <td>Handlebars</td>
  <td>.hbs</td>
  <td>"com.gilt" %% "handlebars-scala" % "2.0.1"</td>
  <td>io.fintrospect.templating.RenderHandlebarsView</td>
</tr>
<tr>
  <td>Mustache (v2.11 only)</td>
  <td>.mustache</td>
  <td>"com.github.spullara.mustache.java" % "compiler" % "0.9.1"<br/>"com.github.spullara.mustache.java" % "scala-extensions-2.11" % "0.9.1"</td>
  <td>io.fintrospect.templating.RenderMustacheView</td>
</tr>
</table>

##Static content
Files can be served easily by using a StaticModule:
```
val publicModule = StaticModule(Root / "public", "public")
```