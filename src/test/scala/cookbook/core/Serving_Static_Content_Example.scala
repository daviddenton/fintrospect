package cookbook.core

import io.fintrospect.ResourceLoader

// fintrospect-core
object Serving_Static_Content_Example extends App {

  import com.twitter.finagle.Http
  import com.twitter.finagle.http.path.Root
  import com.twitter.util.Await.ready
  import io.fintrospect.StaticModule

  val module: StaticModule = StaticModule(Root, ResourceLoader.Directory("."))

  ready(Http.serve(":9999", module.toService))
}

//curl http://localhost:9999/package.json