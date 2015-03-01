Fintrospect [![Build Status](https://travis-ci.org/daviddenton/fintrospect.svg)](https://travis-ci.org/daviddenton/fintrospect) [![Coverage Status](https://coveralls.io/repos/daviddenton/fintrospect/badge.svg?branch=master)](https://coveralls.io/r/daviddenton/fintrospect?branch=master) [![Download](https://api.bintray.com/packages/daviddenton/maven/fintrospect/images/download.svg) ](https://bintray.com/daviddenton/maven/fintrospect/_latestVersion) [ ![Watch](https://www.bintray.com/docs/images/bintray_badge_color.png) ](https://bintray.com/daviddenton/maven/fintrospect/view?source=watch)
===========

Library that adds self-documentation to Finagle server endpoint services.

###Get it:
Add the following lines to ```build.sbt```. Note that this library doesn't depend on a particular version of Finagle,
although it has only been tested with the version below:

```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"

libraryDependencies += "com.twitter" %% "finagle-http" % "6.24.0"

libraryDependencies += "io.github.daviddenton" %% "fintrospect" % "0.0.1"
```

###See it:
See the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples).
