# installation

Fintrospect is intentionally dependency-lite by design - other than Finagle, the core library itself only has a single non `org.scala` dependency.

To activate some optional features, additional dependencies may be required - these are shown in the subsequent sections depending on the use-case.

### core

Add the following lines to ```build.sbt``` - the lib is hosted in Maven Central and JCenter:
```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"
libraryDependencies += "io.fintrospect" %% "fintrospect-core" % "13.11.0"
```
