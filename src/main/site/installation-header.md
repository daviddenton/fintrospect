# installation

Fintrospect is intentionally dependency-lite by design - other than Finagle, the core library itself only has a single dependency.

To activate some optional features, additional dependencies may be required - these are shown in the subsequent sections depending on the use-case.

### core

Add the following lines to ```build.sbt``` - the lib also hosted in Maven Central, but we prefer Bintray):
```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"
libraryDependencies += "io.fintrospect" %% "fintrospect-core" % "13.0.0"
```

```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"
libraryDependencies += "io.fintrospect" %% "fintrospect-core" % "13.0.0"
```
