+++
title = "multipart forms"
tags = ["form", "web", "multipart", "contract"]
categories = ["recipe"]
+++

```scala
// fintrospect-core
object MultiPart_Form_Example extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.{Body, Form, FormField, MultiPartFile}
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val usernameField = FormField.required.string("user")
  val fileField = FormField.required.file("data")
  val form: Body[Form] = Body.multiPartForm(usernameField, fileField)

  val svc: Service[Request, Response] = Service.mk[Request, Response] {
    req => {
      val postedForm: Form = form <-- req
      val name: String = usernameField <-- postedForm
      val data: MultiPartFile = fileField <-- postedForm
      Ok(s"$name posted ${data.filename} which is ${data.length}" + " bytes")
    }
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(form)
    .at(Post) bindTo svc

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
```