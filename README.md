Fintrospect [![Build Status](https://travis-ci.org/daviddenton/fintrospect.svg)](https://travis-ci.org/daviddenton/fintrospect) [![Coverage Status](https://coveralls.io/repos/daviddenton/fintrospect/badge.svg?branch=master)](https://coveralls.io/r/daviddenton/fintrospect?branch=master) [![Download](https://api.bintray.com/packages/daviddenton/maven/fintrospect/images/download.svg) ](https://bintray.com/daviddenton/maven/fintrospect/_latestVersion) [ ![Watch](https://www.bintray.com/docs/images/bintray_badge_color.png) ](https://bintray.com/daviddenton/maven/fintrospect/view?source=watch)
===========

Library that adds self-documentation to Finagle server endpoint services.

###Get it:
Add the following lines to ```build.sbt```. Note that this library doesn't depend on a particular version of Finagle,
and it has only been tested with the version below:

```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"

libraryDependencies += "com.twitter" %% "finagle-http" % "6.25.0"

libraryDependencies += "io.github.daviddenton" %% "fintrospect" % "X.X.X"
```

###See it:
See the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples).

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

