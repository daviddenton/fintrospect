Fintrospect [![Build Status](https://api.travis-ci.org/daviddenton/fintrospect.svg)](https://travis-ci.org/daviddenton/fintrospect) [![Coverage Status](https://coveralls.io/repos/daviddenton/fintrospect/badge.svg?branch=master)](https://coveralls.io/r/daviddenton/fintrospect?branch=master) [![Download](https://api.bintray.com/packages/daviddenton/maven/fintrospect/images/download.svg) ](https://bintray.com/daviddenton/maven/fintrospect/_latestVersion) [ ![Watch](https://www.bintray.com/docs/images/bintray_badge_color.png) ](https://bintray.com/daviddenton/maven/fintrospect/view?source=watch)
===========

Fintrospect is a bolt-on library for use with the [Finagle](http://twitter.github.io/finagle/) RPC framework from Twitter. It was developed out of a desire to provide a pleasant API for describing HTTP request routing in combination with statically-typed documentation which could be verified at compile time and auto-generated at runtime (and thus avoiding the stale documentation risk that exists with manually written docs).

Using this library, you can:
- Automatically generate documentation in a variety of formats (e.g. [Swagger](http://swagger.io/) v1.2 and v2.0). Pluggable architecture for adding your own renderers (currently JSON-based only).
- Define individual HTTP routes and compose them into sensible context-based modules.
- Declare both required and optional parameters to be used in the following locations: ```Path/Header/Query/Form/Body```. Retrieval of the parameters is simple and type-safe (```[T]``` for required, ```Option[T]``` for optional).
- Endpoints automatically verify the prescence and validity of both optional and required parameters (apart from the body for obvious reasons). If any parameters are missing or invalid, a ```BAD_REQUEST``` response is generated - meaning that no extra validation code is required for these parameters in your controller code.
- The library provide identification HTTP headers for dynamic-path based endpoints, removing all dynamic path elements. This allows, for example, calls to particular endpoints to be grouped for metric purposes. e.g. ```/search/author/rowling -> /search/author/{name}```.

###Get it
Add the following lines to ```build.sbt```. Note that this library doesn't depend on a particular version of Finagle,
and it has only been tested with the version below:

```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"
libraryDependencies += "com.twitter" %% "finagle-http" % "6.25.0"
libraryDependencies += "io.github.daviddenton" %% "fintrospect" % "X.X.X"
```

###See it
See the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples).

###Learn it
Adding Fintrospect routes to a Finagle HTTP server is simple. For this example, we'll imagine a Library application (see the example above for the full code) which will be rendering Swagger v2 documentation.
#####Define a module to live at ```http://<host>:8080/library```
This module will have a single endpoint ```search```:

```scala
val apiInfo = ApiInfo("Library Example", "1.0", Some("Simple description"))
val renderer = Swagger2dot0Json(apiInfo) // choose your renderer implementation
val libraryModule = FintrospectModule(Root / "library", renderer)
    .withRoute(new BookSearch(new BookRepo()).route)
val service = FintrospectModule.toService(libraryModule)
Http.serve(":8080", new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service))
```

#####Define the endpoint
This example is quite contrived (and almost all the code is optional) but shows the kind of thing that can be done. Note the use of the example response object, which will be broken down to provide the JSON model for the Swagger documentation.

```scala
class BookSearch(books: Books) {
  private val MAX_PAGES = Query.optional.int("maxPages", "max number of pages in book")
  private val MIN_PAGES = Form.required.int("minPages", "min number of pages in book")
  private val TITLE_TERM = Path.string("term", "the part of the title to look for")

  private def search(titleTerm: String) = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      Ok(array(books.search(MIN_PAGES.from(request), MAX_PAGES.from(request).getOrElse(Integer.MAX_VALUE), titleTerm).map(_.toJson)))
    }
  }

  val route = DescribedRoute("search for books")
    .taking(maxPages)
    .taking(minPages)
    .taking(titleTerm)
    .returning(OK -> "search results", array(Book("a book", "authorName", 99).toJson))
    .returning(BAD_REQUEST -> "invalid request")
    .producing(APPLICATION_JSON)
    .at(POST) / "search" / TITLE_TERM bindTo search
}
```

#####View the generated documentation
The auto-generated documenation lives at the root of the module, so point the Swagger UI at ```http://<host>:8080/library``` to see it.

#####Test it
Fintrospect ships with a testing trait ```TestingFintrospectRoute```, which you can mix into your tests in order to validate your routes.

###Migration Guide

#####v2.X -> v3.X
Migrated away from the in-built Twitter HTTP Request package (com.twitter.finagle.http) and onto the Netty HttpRequest
(org.jboss.netty.handler.codec.http). This is to provide compatibility with the changes to the Finagle APIs in regards
to creating both servers and clients. It also has the advantage of unifying the client/server interface (previously it
was different between the 2). The only things that should have to change in your code are:

  - How servers are created - the new method is simpler (see the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).
  - The signature of routes is now ```Service[HttpRequest,HttpResponse]```. Since the Twitter Request/Response classes
   extends these interfaces, usages of the ResponseBuilder remain the same.
  - Form-based parameters are now defined with the ```Form``` object, and not the ```Query``` object (which now just retrieves Query String parameters from the URL).
