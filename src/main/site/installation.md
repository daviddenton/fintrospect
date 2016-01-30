# installation

Add the following lines to ```build.sbt```. Note that while the library doesn't depend on a particular version of Finagle,
 it has built against the version below:

```scala
resolvers += "JCenter" at "https://jcenter.bintray.com"
libraryDependencies += "com.twitter" %% "finagle-http" % "6.31.0"
libraryDependencies += "io.github.daviddenton" %% "fintrospect" % "12.0.2"
```

Additionally, to activate any message formats other than Argo JSON and native XML you'll need to import the relevant libraries (e.g. Json4S native/jackson), since only the bindings are included in the Fintrospect JAR.
