<h1>
<a href="http://fintrospect.io">Fintrospect</a>&nbsp;&nbsp;&nbsp;
<a href="https://bintray.com/daviddenton/maven/fintrospect/_latestVersion" target="_top"><img src="https://api.bintray.com/packages/daviddenton/maven/fintrospect/images/download.svg"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://travis-ci.org/daviddenton/fintrospect" target="_top"><img src="https://travis-ci.org/daviddenton/fintrospect.svg?branch=master"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://coveralls.io/github/daviddenton/fintrospect?branch=master" target="_top"><img src="https://coveralls.io/repos/daviddenton/fintrospect/badge.svg?branch=master"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://gitter.im/daviddenton/fintrospect" target="_top"><img src="https://img.shields.io/gitter/room/nwjs/nw.js.svg"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://bintray.com/daviddenton/maven/fintrospect/view?source=watch" target="_top"><img height="45" src="https://www.bintray.com/docs/images/bintray_badge_color.png"/></a>&nbsp;&nbsp;&nbsp;
</h1>

Fintrospect is a library that adds an intelligent HTTP routing layer to the 
<a href="http://twitter.github.io/finagle/" target="_top">Finagle</a> RPC framework from Twitter. It provides a simple way to 
implement contracts for both server and client-side HTTP services which are:

- ```Type-safe``` : auto-marshalls all request parameters/bodies into the correct types (including primitives + JSON/XML etc...)
- ```Auto-validating``` : the presence of required and optional request parameters and bodies are checked before entering service-layer code
- ```Auto-documenting``` : runtime generation of endpoint documentation such as <a href="http://swagger.io/" target="_top">Swagger</a> JSON or web sitemap XML. 
Generates <a href="http://json-schema.org/" target="_top">JSON Schema</a> for example object formats to be included in these API docs.
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

## Get it
Fintropect is intentionally dependency-lite by design - the library itself only has a single dependency. The other requirement is Finagle
itself - the choice of version is up to the user (although it has built against the version shown). 

To activate some optional features, additional dependencies may be required - please see <a href="http://fintrospect.io/installation">here</a> for details.

Add the following lines to ```build.sbt```:

```scala
libraryDependencies += "com.twitter" %% "finagle-http" % "6.31.0"
libraryDependencies += "io.github.daviddenton" %% "fintrospect" % "12.1.0"
```

## See the code
See a <a href="https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples" target="_top">full example application</a>.

## Learn it
See the full user guide <a href="http://fintrospect.io/" target="_top">here</a>, or read on for the tldr; example. :)

### Server-side contracts
Adding Fintrospect routes to a Finagle HTTP server is simple. For this example, we'll imagine a Library application (see the example 
above for the full code) which will be rendering Swagger v2 documentation.

#### Define the endpoint
This example is quite contrived (and almost all the code is optional) but shows the kind of thing that can be done. Note the use of the 
example response object, which will be broken down to provide the JSON model for the Swagger documentation. 

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

  val theCall = formClient( gender --> "female", 
                            body --> Form(theDate --> LocalDate.of(2015, 1, 1)), 
                            theUser --> System.getenv("USER"))

  println(Await.result(theCall))
```

## Upgrading?
See the <a href="https://github.com/daviddenton/fintrospect/blob/master/CHANGELOG.md" target="_top">changelog</a>.

## Contributing
There are many ways in which you can contribute to the development of the library:

- Give us a ⭐️ on Github - you know you want to ;)
- Questions can be directed towards the Gitter channel, or on Twitter <a href="https://twitter.com/fintrospectdev">@fintrospectdev</a>
- For issues, please describe giving as much detail as you can - including version and steps to recreate

See the <a href="https://github.com/daviddenton/fintrospect/blob/master/CONTRIBUTING.md"/>contributor guide</a> for details.