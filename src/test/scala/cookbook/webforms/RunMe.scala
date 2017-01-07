package cookbook.webforms

import com.twitter.util.Future

// fintrospect-core
object RunMe extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.{Body, Form, FormField}
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  val nameField = FormField.required.string("name")
  val ageField = FormField.optional.int("age")
  val form = Body.webForm(nameField -> "everyone has a name!", ageField -> "age is an int!")

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
    val name: String = nameField <-- postedForm
    val age: Option[Int] = ageField <-- postedForm
    Ok(s"$name is ${age.map(_.toString).getOrElse("too old to admit it")}")
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(form)
    .at(Post) bindTo svc

  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
