package cookbook.multipartforms

// fintrospect-core
object RunMe extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.{Body, FormField, MultiPartFile}
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  import scala.language.reflectiveCalls

  val usernameField = FormField.required.string("user")
  val fileField = FormField.required.file("data")
  val form = Body.multiPartForm(usernameField, fileField)

  val svc: Service[Request, Response] = Service.mk[Request, Response] {
    req => {
      val postedForm = form <-- req
      val name: String = usernameField <-- postedForm
      val data: MultiPartFile = fileField <-- postedForm
      Ok(s"$name posted ${data.filename} which is ${data.length}" + " bytes")
    }
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(form)
    .at(Post) bindTo svc

  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
