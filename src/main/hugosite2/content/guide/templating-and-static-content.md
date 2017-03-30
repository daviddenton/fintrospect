+++
title = "templating & static content"
weight = 7
+++

# templating
Templates are applied by using a custom ```RenderView``` filter to convert ```View``` instances into standard Http Responses. Simply implement the 
```View``` trait and then put a matching template file onto the classpath, and chain the output of the model-creating ```Service``` into 
the filter. You can do this for entire modules by making the ```RouteModule``` itself generified on ```View``` by using the 
templating ```Filter``` as a Module-level filter:

```scala
case class ViewMessage(value: String) extends View

val showMessage = Service.mk[Request, View] { _ => Future(ViewMessage("some value to be displayed")) }

val renderer = if(devMode) MustacheTemplates.HotReload("src/main/resources") else MustacheTemplates.CachingClasspath(".")

val webModule = RouteModule(Root / "web",
    new SiteMapModuleRenderer(new URL("http://root.com")),
    new RenderView(Html.ResponseBuilder, renderer))
    .withRoute(RouteSpec().at(Get) / "message" bindTo showMessage)
```

## redirects
After Form posts, it might be desirable to return an HTTP redirect instead of a View in the case of success. 
For this purpose, use an instance of the `View.Redirect` class. The location and the status code (default 303) are configurable:
```scala
val redirect = Service.mk[Request, View] { _ => Future(View.Redirect("http://my.server/myRoute")) }
```

Available implementations of the `TemplateRenderer` are (see the relevant implementation of `Templates`):
- cached from the classpath
- cached from the filesystem
- hot-reloading from the filesystem

Similarly to how the ```ResponseBuilder``` codecs work, no 3rd-party dependencies are bundled with Fintrospect - simply import the extra SBT dependencies 
as required:

INSERT LINK TO FORMAT TABLE HERE

# static content
Static files can easily be served from the either the Classpath or a Directory by using an instance of ```StaticModule``` with an 
appropriately injected ```ResourceLoader```:
```scala
val cpModule = StaticModule(Root / "public", ResourceLoader.Classpath("package/path"))
val dirModule = StaticModule(Root / "public", ResourceLoader.Directory("file/dir/path"))
```

Note that due to security concerns, `ResourceLoader.Classpath` should be configured to use a package which does NOT overlap with class assets.

<a class="next" href="/guide/testing"><button type="button" class="btn btn-sm btn-default">next: testing</button></a>


