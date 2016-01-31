# templating
Templates are applied by using a custom ```Filter``` to convert ```View``` instances into standard Http Responses. Simply implement the 
```View``` trait and then put a matching template file onto the classpath, and chain the output of the model-creating ```Service``` into 
the ```Filter```. You can do this for entire modules by making the ```ModuleSpec``` itself generified on ```View``` and using the 
templating ```Filter``` as a Module-level filter:

```
case class ViewMessage(value: String) extends View

def showMessage() = Service.mk[Request, View] { _ => Future.value(ViewMessage("some value to be displayed")) }

val webModule = ModuleSpec[View](Root / "web",
    new SiteMapModuleRenderer(new URL("http://root.com")),
    new RenderMustacheView(Html.ResponseBuilder))
    .withRoute(RouteSpec().at(Get) / "message" bindTo showMessage)
```

Similarly to how the ```ResponseBuilder``` works, no 3rd-party dependencies are bundled with Fintrospect - simply import the extra SBT dependencies 
as required:
