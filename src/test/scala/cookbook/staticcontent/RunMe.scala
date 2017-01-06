package cookbook.staticcontent


object RunMe extends App {

  import com.twitter.finagle.Http
  import com.twitter.finagle.http.path.Root
  import com.twitter.util.Await.ready
  import io.fintrospect.ResourceLoader.Directory
  import io.fintrospect.StaticModule

  val module: StaticModule = StaticModule(Root, Directory("."))

  ready(Http.serve(":9999", module.toService))
}
