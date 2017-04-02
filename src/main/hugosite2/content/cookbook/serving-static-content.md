+++
title = "serving static content"
tags = ["web", "static"]
categories = ["fintrospect-core"]
intro = ""
+++

```scala


object Serving_Static_Content_Example extends App {

  import com.twitter.finagle.Http
  import com.twitter.finagle.http.path.Root
  import com.twitter.util.Await.ready
  import io.fintrospect.{Module, ResourceLoader, StaticModule}

  val module: Module = StaticModule(Root, ResourceLoader.Directory("."))

  ready(Http.serve(":9999", module.toService))
}

//curl -v http://localhost:9999/package.json
```