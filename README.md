<b>Important note for upgraders from v`12.X.X` and below to v`13.X.X`. Fintrospect has changed top-level Maven group name to `io.fintrospect`. Please refer to the  <a href="http://fintrospect.io/changelog">changelog</a> for upgrade notes</b>.

<h1>
<a href="http://fintrospect.io">Fintrospect</a>&nbsp;&nbsp;&nbsp;
<a href="https://bintray.com/fintrospect/maven/fintrospect-core/_latestVersion"><img src="https://api.bintray.com/packages/fintrospect/maven/fintrospect-core/images/download.svg"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://travis-ci.org/daviddenton/fintrospect"><img src="https://travis-ci.org/daviddenton/fintrospect.svg?branch=master"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://coveralls.io/github/daviddenton/fintrospect?branch=master"><img src="https://coveralls.io/repos/daviddenton/fintrospect/badge.svg?branch=master"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://gitter.im/daviddenton/fintrospect"><img src="https://badges.gitter.im/daviddenton/fintrospect.svg"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://bintray.com/daviddenton/maven/fintrospect/view?source=watch"><img height="45" src="https://www.bintray.com/docs/images/bintray_badge_color.png"/></a>&nbsp;&nbsp;&nbsp;
</h1>

Fintrospect is a library that adds an intelligent HTTP routing layer to the 
<a href="http://twitter.github.io/finagle/">Finagle</a> RPC framework from Twitter. It provides a simple way to 
implement contracts for both server and client-side HTTP services which are:

- ```Type-safe``` : auto-marshalls all request parameters/bodies into the correct types (including primitives + JSON/XML etc...)
- ```Auto-validating``` : the presence of required and optional request parameters and bodies are checked before entering service-layer code
- ```Auto-documenting``` : runtime generation of endpoint documentation such as <a href="http://swagger.io/">Swagger</a> JSON or web sitemap XML. 
Generates <a href="http://json-schema.org/">JSON Schema</a> for example object formats to be included in these API docs.
- ```Uniform``` : reuse the same contract to define both incoming or outgoing Finagle HTTP services. This also allows extremely low effort fake servers to be created

Additionally, Fintrospect provides a number of mechanisms to leverage these routes:

- Easily build type-safe HTTP responses with a set of custom builders for a wide variety of message formats:
  - JSON: <a href="http://argo.sourceforge.net/">Argo</a>, <a href="http://argonaut.io/">Argonaut</a>, 
  <a href="https://github.com/travisbrown/circe">Circe</a>, <a href="https://github.com/google/gson">GSON</a>, 
  <a href="http://json4s.org/">Json4S</a>, <a href="https://github.com/playframework">Play JSON</a>, 
  <a href="https://github.com/spray/spray-json">Spray JSON</a>
    - Auto-marshaling of case classes instances to/from JSON (for `Argonaut`/`Circe`/`Json4S`/`Play`).
    - Implement simple `PATCH`/`PUT` endpoints of case class instances (`Circe` only).
  - Native implementations of XML, Plain Text, HTML, XHTML
  - <a href="http://msgpack.org">MsgPack</a> binary format
- Serve static content from the classpath or a directory
- Template ```View``` support (with Hot-Reloading) for building responses with <a href="http://mustache.github.io/">Mustache</a> or <a href="http://handlebarsjs.com">Handlebars</a>
- Anonymising headers for dynamic-path based endpoints, removing all dynamic path elements. This allows, for example, calls to particular endpoints to be grouped for metric purposes. e.g. 
```/search/author/rowling``` becomes ```/search/author/{name}```
- Interacts seamlessly with other Finagle based libraries, such as <a href="https://github.com/finagle/finagle-oauth2">Finagle OAuth2</a> 
- Utilities to help you unit-test endpoint services and write HTTP contract tests for remote dependencies 

## Get it
Fintrospect is intentionally dependency-lite by design - other than Finagle, the core library itself only has a single non `org.scala` dependency.

To activate some optional features, additional dependencies may be required - please see <a href="http://fintrospect.io/installation">here</a> for details.

Add the following lines to ```build.sbt``` - the lib is hosted in Maven Central and JCenter:
```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"
libraryDependencies += "io.fintrospect" %% "fintrospect-core" % "13.10.1"
```

## See the code
See the examples in this repo, or clone the <a href="http://github.com/daviddenton/fintrospect-example-app">full example application repo</a>.

## Learn it
See the full user guide <a href="http://fintrospect.io/">here</a>, or read on for the tldr; example. :)

### Server-side contracts
Adding Fintrospect routes to a Finagle HTTP server is simple. For this example, we'll imagine a Library application (see the example 
above for the full code) which will be rendering Swagger v2 documentation.

#### Define the endpoint
This example is quite contrived (and almost all the code is optional) but shows the kind of thing that can be done. Note the use of the 
example response object, which will be broken down to provide the JSON model for the Swagger documentation. 

```scala
// implicit conversion from Status -> ResponseBuilder pulled in here
import io.fintrospect.formats.Argo.ResponseBuilder.implicits._
import io.fintrospect.formats.Argo.JsonFormat.array

class BookSearch(books: Books) {
  private val maxPages = Query.optional.int("maxPages", "max number of pages in book")
  private val minPages = FormField.optional.int("minPages", "min number of pages in book")
  private val titleTerm = FormField.required.string("term", "the part of the title to look for")
  private val form = Body.form(minPages, titleTerm)

  private def search() = Service.mk[Request, Response] { 
    request => {
      val requestForm = form.from(request)
      Status.Ok(array(
        books.search(
            minPages.from(requestForm).getOrElse(MIN_VALUE), 
            maxPages.from(request).getOrElse(MAX_VALUE),
            titleTerm.from(requestForm)
        ).map(_.toJson)))
    }
  }

  val route = RouteSpec("search for books")
    .taking(maxPages)
    .body(form)
    .returning(Status.Ok -> "we found your book", array(Book("a book", "authorName", 99).toJson))
    .returning(Status.BadRequest -> "invalid request")
    .producing(ContentTypes.APPLICATION_JSON)
    .at(Method.Post) / "search" bindTo search
}
```

#### Define a module to live at ```http://{host}:8080/library```
This module will have a single endpoint ```search```:

```scala
val apiInfo = ApiInfo("Library Example", "1.0", Option("Simple description"))
val renderer = Swagger2dot0Json(apiInfo) 
val libraryModule = ModuleSpec(Root / "library", renderer)
    .withRoute(new BookSearch(new BookRepo()).route)
val service = Module.toService(libraryModule)
Http.serve(":8080", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)) 
```

#### View the generated documentation
The auto-generated documentation lives at the root of the module, so point the Swagger UI at ```http://{host}:8080/library``` to see it.

### Client-side contracts
Declare the fields to be sent to the client service and then bind them to a remote service. This produces a simple function, which can 
then be called with the bindings for each parameter.

Since we can re-use the routes between client and server, we can easily create fake implementations of remote systems without having to 
redefine the contract. This means that marshalling of objects and values into/out of the HTTP messages can be reused.
```scala
  val theDate = Path.localDate("date")
  val gender = FormField.optional.string("gender")
  val body = Body.form(gender)

  val sharedRouteSpec = RouteSpec()
    .body(body)
    .at(Get) / "firstSection" / theDate

  val fakeServerRoute = sharedRouteSpec bindTo (dateFromPath => Service.mk[Request, Response] {
    request: Request => {
      // insert stub server implementation in here
      println("Form sent was " + (body <-- request))
      Ok(dateFromPath.toString)
    }
  })

  Await.result(new TestHttpServer(10000, fakeServerRoute).start())

  val client = sharedRouteSpec bindToClient Http.newService("localhost:10000")

  val theCall = client(
    body --> Form(gender --> "male"), 
    theDate --> LocalDate.of(2015, 1, 1)
  )

  println(Await.result(theCall))
```

## Upgrading?
See the <a href="https://github.com/daviddenton/fintrospect/blob/master/CHANGELOG.md">changelog</a>.

## Contributing
There are many ways in which you can contribute to the development of the library:

- Give us a ⭐️ on Github - you know you want to ;)
- Questions can be directed towards the Gitter channel, or on Twitter <a href="https://twitter.com/fintrospectdev">@fintrospectdev</a>
- For issues, please describe giving as much detail as you can - including version and steps to recreate

See the <a href="https://github.com/daviddenton/fintrospect/blob/master/CONTRIBUTING.md"/>contributor guide</a> for details.
