Fintrospect
===========
<a href="https://travis-ci.org/daviddenton/fintrospect" target="_top"><img src="https://travis-ci.org/daviddenton/fintrospect.svg?branch=master"/></a>
<a href="https://coveralls.io/github/daviddenton/fintrospect?branch=master" target="_top"><img src="https://coveralls.io/repos/daviddenton/fintrospect/badge.svg?branch=master"/></a>
<a href="https://bintray.com/daviddenton/maven/fintrospect/_latestVersion" target="_top"><img src="https://api.bintray.com/packages/daviddenton/maven/fintrospect/images/download.svg"/></a>
<a href="https://bintray.com/daviddenton/maven/fintrospect/view?source=watch" target="_top"><img src="https://www.bintray.com/docs/images/bintray_badge_color.png"/></a>

Fintrospect is a bolt-on HTTP routing library for use with the <a href="http://twitter.github.io/finagle/" target="_top">Finagle</a>  RPC framework from Twitter. It was developed out of a desire to provide a pleasant API for describing HTTP request routing in combination with statically-typed documentation which could be verified at compile time and auto-generated at runtime (and thus avoiding the stale documentation risk that exists with manually written docs).

Using this library, you can:
- Define individual HTTP routes and compose them into sensible context-based modules.
- Declare both required and optional parameters to be used in the following locations: ```Path/Header/Query/Form/Body```. Retrieval of the parameters is simple and type-safe (```[T]``` for required, ```Option[T]``` for optional). Custom datatypes
for parameters are supported. Also support for typesafe conversions of custom types.
- Automatically generate documentation in a variety of formats (e.g. <a href="http://swagger.io/" target="_top">Swagger</a> v1.1 and v2.0). Pluggable architecture for adding your own format renderers (e.g other JSON, XML).
- Endpoints automatically verify the presence and validity of both optional and required parameters. If any parameters are missing or invalid, a ```BAD_REQUEST``` response is generated - meaning that no extra validation code is required for these parameters in your controller code.
- The library provide identification HTTP headers for dynamic-path based endpoints, removing all dynamic path elements. This allows, for example, calls to particular endpoints to be grouped for metric purposes. e.g. ```/search/author/rowling -> /search/author/{name}```.
- Define HTTP Client endpoints APIs which reuse the same syntax and parameter bindings as the server-side, which means that you can use the same route specification to define both sides of the transport boundary. This allows, for example,
the HTTP API of a downstream servers (for a fake) to be created with no effort, and exactly matching the client side. These endpoints are also exposed as simple functions.
- A set of HTTP Response builders with pluggable extension points for custom formats; currently supported are:
  - Argo JSON (Java)
  - Argonaut JSON (v6.0.X compatible) (v10.1.X+)
  - Json4s (v3.2.X+ compatible) Native & Jackson (v9+)
  - Play JSON (v2.4.X compatible) (v10.2.X+)
  - Spray JSON (v9+)
  - Scala native XML

###Get it
Add the following lines to ```build.sbt```. Note that this library doesn't depend on a particular version of Finagle,
and it has built against the version below:

```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"
libraryDependencies += "com.twitter" %% "finagle-http" % "6.30.0"
libraryDependencies += "io.github.daviddenton" %% "fintrospect" % "X.X.X"
```

Additionally, to activate any message formats other than Argo JSON and native XML you'll need to import the relevant libraries (e.g. Json4S native/jackson), since only the bindings are included in the Fintrospect JAR.

###See it
See the <a href="https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples" target="_top">example code</a>.

###Learn it

####Server-side
Adding Fintrospect routes to a Finagle HTTP server is simple. For this example, we'll imagine a Library application (see the example above for the full code) which will be rendering Swagger v2 documentation.
#####Define a module to live at ```http://{host}:8080/library```
This module will have a single endpoint ```search```:

```scala
val apiInfo = ApiInfo("Library Example", "1.0", Option("Simple description"))
val renderer = Swagger2dot0Json(apiInfo) // choose your renderer implementation
val libraryModule = FintrospectModule(Root / "library", renderer)
    .withRoute(new BookSearch(new BookRepo()).route)
val service = FintrospectModule.toService(libraryModule)
Httpx.serve(":8080", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service))
```

#####Define the endpoint
This example is quite contrived (and almost all the code is optional) but shows the kind of thing that can be done. Note the use of the example response object, which will be broken down to provide the JSON model for the Swagger documentation.

```scala
class BookSearch(books: Books) {
  private val maxPages = Query.optional.int("maxPages", "max number of pages in book")
  private val minPages = FormField.optional.int("minPages", "min number of pages in book")
  private val titleTerm = FormField.required.string("term", "the part of the title to look for")
  private val form = Body.form(minPages, titleTerm)

  private def search() = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      val requestForm = form.from(request)
      OK(array(books.search(minPages.from(requestForm).getOrElse(MIN_VALUE),
        maxPages.from(request).getOrElse(MAX_VALUE),
        titleTerm.from(requestForm)).map(_.toJson)))
    }
  }

  val route = RouteSpec("search for books")
    .taking(maxPages)
    .body(form)
    .returning(Ok -> "we found your book", array(Book("a book", "authorName", 99).toJson))
    .returning(BadRequest -> "invalid request")
    .producing(APPLICATION_JSON)
    .at(Post) / "search" bindTo search
}
```

#####View the generated documentation
The auto-generated documentation lives at the root of the module, so point the Swagger UI at ```http://{host}:8080/library``` to see it.

####Client-side
Declare the fields to be sent to the client service and then bind them to a remote service. This produces a simple function, which can then be called with the bindings for each parameter.
```scala
  val httpClient = Http.newService("localhost:10000")

  val theUser = Path.string("user")
  val gender = Header.required.string("gender")
  val theDate = FormField.required.localDate("date")
  val body = Body.form(theDate)

  val formClient = RouteSpec()
    .taking(gender)
    .body(body)
    .at(Get) / "firstSection" / theUser bindToClient httpClient

  val theCall = formClient(gender --> "female", body --> Form(theDate --> LocalDate.of(2015, 1, 1)), theUser --> System.getenv("USER"))

  println(Await.result(theCall))
```

#####Test it
Fintrospect ships with a testing trait ```TestingFintrospectRoute```, which you can mix into your tests in order to validate your declared server-side routes.

###Upgrading?
See the <a href="https://github.com/daviddenton/fintrospect/blob/master/RELEASE.md" target="_top">Roadmap</a>.

###Contributions
If there are any message format libraries bindings that you'd like to see included, then please feel free to suggest them or provide a PR. For JSON formats, this
is particularly easy to implement - just provide an implementation of ```JsonLibrary``` by following the ```Argo``` example in the source.