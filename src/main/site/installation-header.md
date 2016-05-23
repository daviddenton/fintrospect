# installation

Fintrospect is intentionally dependency-lite by design - the library itself only has a single dependency. The other requirement is Finagle
itself - the choice of version is up to the user (although it has built against the version shown). 

To activate some optional features, additional dependencies may be required - these are shown in the subsequent sections depending on the use-case.

### core
Add the following lines to ```build.sbt```:

```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"
libraryDependencies += "com.twitter" %% "finagle-http" % "6.35.0"
libraryDependencies += "io.github.daviddenton" %% "fintrospect" % "12.18.1"
```
