package cookbook.json_libraries


case class Profile(name: String, age: Option[Int])

// fintrospect-core
// fintrospect-circe
object Full_Auto_Marshalling_Example extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.Future
  import io.circe.generic.auto._
  import io.fintrospect.formats.Circe
  import io.fintrospect.formats.Circe.Auto._
  import io.fintrospect.parameters.Body
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val insultMe: Service[Profile, Profile] = Service.mk[Profile, Profile] {
    inProfile => Future(inProfile.copy(name = inProfile.name + " Smells"))
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(Body(Circe.bodySpec[Profile]()))
    .at(Post) bindTo InOut(insultMe)

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v -XPOST http://localhost:9999/ --data '{"name":"David", "age": 50}'