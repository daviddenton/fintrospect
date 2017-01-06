package cookbook.multipartwebforms


object RunMe extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.Future
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.{Body, Form, FormField, MultiPartFile}
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  import scala.language.reflectiveCalls

  val usernameField = FormField.required.string("user")
  val fileField = FormField.required.file("data")
  val form = Body.multiPartWebForm(usernameField -> "everyone has a name!", fileField -> "file is required!")

  val svc: Service[Request, Response] = Service.mk[Request, Response] {
    req => {
      val postedForm: Form = form <-- req
      if (postedForm.isValid) successMessage(postedForm) else failureMessage(postedForm)
    }
  }

  def failureMessage(postedForm: Form): Future[Response] = {
    val errorString = postedForm.errors.map(e => e.param.name + ": " + e.reason).mkString("\n")
    BadRequest("errors were: " + errorString)
  }

  def successMessage(postedForm: Form): Future[Response] = {
    val name: String = usernameField <-- postedForm
    val data: MultiPartFile = fileField <-- postedForm
    Ok(s"$name posted ${data.filename} which is ${data.length}" + " bytes")
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(form)
    .at(Post) bindTo svc

  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
