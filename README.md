<h1 class="githubonly">Fintrospect</h1>
<a class="githubonly" href="https://travis-ci.org/daviddenton/fintrospect" target="_top"><img src="https://travis-ci.org/daviddenton/fintrospect.svg?branch=master"/></a>
<a class="githubonly" href="https://coveralls.io/github/daviddenton/fintrospect?branch=master" target="_top"><img src="https://coveralls.io/repos/daviddenton/fintrospect/badge.svg?branch=master"/></a>
<a class="githubonly" href="https://bintray.com/daviddenton/maven/fintrospect/_latestVersion" target="_top"><img src="https://api.bintray.com/packages/daviddenton/maven/fintrospect/images/download.svg"/></a>
<a class="githubonly" href="https://bintray.com/daviddenton/maven/fintrospect/view?source=watch" target="_top"><img src="https://www.bintray.com/docs/images/bintray_badge_color.png"/></a>

Fintrospect a library that adds an intelligent HTTP routing layer to the 
<a href="http://twitter.github.io/finagle/" target="_top">Finagle</a> RPC framework from Twitter. It provides a simple way to 
implement contracts for both server and client-side HTTP services which are:

- ```Type-safe``` : auto-marshalls all request parameters/bodies into the correct types (including primitives + JSON/XML etc...)
- ```Auto-validating``` : the presence of required and optional request parameters and bodies are checked before entering service-layer code
- ```Auto-documenting``` : runtime generation of endpoint documentation such as <a href="http://swagger.io/" target="_top">Swagger</a> JSON or web sitemap XML
- ```Uniform``` : reuse the same contract to define both incoming or outgoing Finagle HTTP services. This also allows extremely low effort fake servers to be created

Additionally, Fintrospect provides a number of mechanisms to leverage these routes:

- Easily build type-safe HTTP responses with a set of custom builders for a wide variety of message formats:
  - JSON: <a href="http://argo.sourceforge.net/" target="_top">Argo</a>, <a href="http://argonaut.io/" target="_top">Argonaut</a>, <a href="https://github.com/travisbrown/circe" target="_top">Circe</a>, <a href="https://github.com/google/gson" 
  target="_top">GSON</a>, <a href="http://json4s.org/" target="_top">Json4S</a>, <a href="https://github.com/google/gson" target="_top">Play JSON</a>, <a href="https://github.com/google/gson" target="_top">Spray JSON</a>
  - Native implementations of XML, Plain Text, HTML, XHTML
- Serve static files from the classpath
- Template ```View``` support for building responses with <a href="http://mustache.github.io/" target="_top">Mustache</a> or <a href="http://handlebarsjs.com" target="_top">Handlebars</a>
- Anonymising headers for dynamic-path based endpoints, removing all dynamic path elements. This allows, for example, calls to particular endpoints to be grouped for metric purposes. e.g. 
```/search/author/rowling``` becomes ```/search/author/{name}```
- Utilities to help you unit-test endpoint services and write HTTP contract tests for remote dependencies 

### Get it
Add the following to ```build.sbt```. Note that this library doesn't depend on a particular version of Finagle, and it has built against the version below:

```scala
libraryDependencies += "com.twitter" %% "finagle-http" % "6.31.0"
libraryDependencies += "io.github.daviddenton" %% "fintrospect" % "12.1.0"
```

In order to keep the core library dependency-lite, some features require additional dependencies. Please see <a href="http://fintrospect.io/installation">here</a> for details.

### See it
See the <a href="https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples/full" target="_top">example code</a>.

### Learn it
See the rest of the <a href="http://fintrospect.io/" target="_top">guide</a>

#### Server-side
Adding Fintrospect routes to a Finagle HTTP server is simple. For this example, we'll imagine a Library application (see the example above for the full code) which will be rendering Swagger v2 documentation.
##### Define a module to live at ```http://{host}:8080/library```
This module will have a single endpoint ```search```:

```scala
val apiInfo = ApiInfo("Library Example", "1.0", Option("Simple description"))
val renderer = Swagger2dot0Json(apiInfo) // choose your renderer implementation
val libraryModule = ModuleSpec(Root / "library", renderer)
    .withRoute(new BookSearch(new BookRepo()).route)
val service = Module.toService(libraryModule)
Http.serve(":8080", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)) // remember to make your own Cors Policy for prod!
```

##### Define the endpoint
This example is quite contrived (and almost all the code is optional) but shows the kind of thing that can be done. Note the use of the example response object, which will be broken down to provide the JSON model for the Swagger documentation.

```scala
class BookSearch(books: Books) {
  private val maxPages = Query.optional.int("maxPages", "max number of pages in book")
  private val minPages = FormField.optional.int("minPages", "min number of pages in book")
  private val titleTerm = FormField.required.string("term", "the part of the title to look for")
  private val form = Body.form(minPages, titleTerm)

  private def search() = Service.mk[Request, Response] { 
    request => {
      val requestForm = form.from(request)
      Ok(array(books.search(minPages.from(requestForm).getOrElse(MIN_VALUE),
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

##### View the generated documentation
The auto-generated documentation lives at the root of the module, so point the Swagger UI at ```http://{host}:8080/library``` to see it.

#### Client-side
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

##### Test it
Fintrospect ships with a testing trait ```TestingFintrospectRoute```, which you can mix into your tests in order to validate your declared server-side routes.

### Upgrading?
See the <a href="https://github.com/daviddenton/fintrospect/blob/master/CHANGELOG.md" target="_top">changelog</a>.

### Contributing
If there are any message format library or templating engine bindings that you'd like to see included, then please feel free to suggest them or provide a PR. For JSON formats, this
is particularly easy to implement - just provide an implementation of ```JsonLibrary``` by following the ```Argo``` example in the source.
