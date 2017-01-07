package cookbook.simpleforms

// fintrospect-core
object RunMe extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.{Body, FormField}
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  val nameField = FormField.required.string("name")
  val ageField = FormField.optional.int("age")
  val form = Body.form(nameField, ageField)

  val svc: Service[Request, Response] = Service.mk[Request, Response] {
    req => {
      val formInstance = form <-- req
      val name: String = nameField <-- formInstance
      val age: Option[Int] = ageField <-- formInstance
      Ok(s"$name is ${age.map(_.toString).getOrElse("too old to admit it")}")
    }
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(form)
    .at(Post) bindTo svc

  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
//curl -v -H"Content-Type: application/x-www-form-urlencoded" -XPOST http://localhost:9999/ --data '&age=asd'
//curl -v -H"Content-Type: application/x-www-form-urlencoded" -XPOST http://localhost:9999/ --data 'name=david&age=12'
