Fintrospect [![Build Status](https://travis-ci.org/daviddenton/fintrospect.svg)](https://travis-ci.org/daviddenton/fintrospect) [![Coverage Status](https://coveralls.io/repos/daviddenton/fintrospect/badge.svg?branch=master)](https://coveralls.io/r/daviddenton/fintrospect?branch=master) [![Download](https://api.bintray.com/packages/daviddenton/maven/fintrospect/images/download.svg) ](https://bintray.com/daviddenton/maven/fintrospect/_latestVersion) [ ![Watch](https://www.bintray.com/docs/images/bintray_badge_color.png) ](https://bintray.com/daviddenton/maven/fintrospect/view?source=watch)
===========

Fintrospect is a bolt-on library for use with the [Finagle](http://twitter.github.io/finagle/) RPC framework from Twitter. It was developed out of a desire to provide a pleasant API for describing HTTP request routing in combination with statically-typed documentation which could be verified at compile time and auto-generated at runtime (and thus avoiding the stale documentation risk that exists with manually written docs).

Using this library, you can:
- Automatically generate documentation in a variety of formats (e.g. [Swagger](http://swagger.io/) v1.2 and v2.0). Pluggable architecture for adding your own renderers (currently JSON-based only).
- Define individual HTTP routes and compose them into sensible context-based modules.
- Declare both mandatory and optional parameters to be used in the following locations: ```Path/Header/Query/Form/Body```. Retrieval of the parameters is simple and type-safe (```[T]``` for mandatory, ```Option[T]``` for optional). 
- Endpoints automatically verify the prescence and validity of both optional and mandatory parameters (apart from the body for obvious reasons). If any parameters are missing or invalid, a ```BAD_REQUEST``` response is generated - meaning that no extra validation code is required for these parameters in your controller code.

###See it:
See the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples).

###Get it:
Add the following lines to ```build.sbt```. Note that this library doesn't depend on a particular version of Finagle,
and it has only been tested with the version below:

```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"
libraryDependencies += "com.twitter" %% "finagle-http" % "6.25.0"
libraryDependencies += "io.github.daviddenton" %% "fintrospect" % "X.X.X"
```

###Migration Guide

####v2.X -> v3.X
Migrated away from the in-built Twitter HTTP Request package (com.twitter.finagle.http) and onto the Netty HttpRequest
(org.jboss.netty.handler.codec.http). This is to provide compatibility with the changes to the Finagle APIs in regards
to creating both servers and clients. It also has the advantage of unifying the client/server interface (previously it
was different between the 2). The only things that should have to change in your code are:

  - How servers are created - the new method is simpler (see the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).
  - The signature of routes is now ```Service[HttpRequest,HttpResponse]```. Since the Twitter Request/Response classes
   extends these interfaces, usages of the ResponseBuilder remain the same.
  - Form-based parameters are now defined with the ```Form``` object, and not the ```Query``` object (which now just retrieves Query String parameters from the URL).
