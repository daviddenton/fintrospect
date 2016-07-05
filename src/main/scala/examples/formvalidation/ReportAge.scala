package examples.formvalidation

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Request
import io.fintrospect.parameters.StringValidation.EmptyIsInvalid
import io.fintrospect.parameters.{Body, Form, FormField, WebForm}
import io.fintrospect.templating.View
import io.fintrospect.templating.View.viewToFuture
import io.fintrospect.{RouteSpec, ServerRoutes}

/**
  * This is a set of 2 routes which model:
  * 1. GET route - form display
  * 2. POST route - submission of form
  */
class ReportAge extends ServerRoutes[Request, View] {

  // displays the initial form to the user
  add(RouteSpec().at(Get) bindTo Service.mk { rq: Request => AgeEntry() })

  private val submit = Service.mk {
    rq: Request => {
      val submitted = AgeEntry.form <-- rq
      if (submitted.isValid) {
        val name = AgeEntry.fields.name <-- submitted.form
        val age = AgeEntry.fields.age <-- submitted.form
        AgeReport(name, age)
      } else {
        AgeEntry(submitted)
      }
    }
  }

  // provides form validation on POST to same route
  add(RouteSpec().body(AgeEntry.form).at(Post) bindTo submit)
}

case class AgeReport(name: String, age: Int) extends View

object AgeEntry {
  object fields {
    val name = FormField.required.string("name", validation = EmptyIsInvalid)
    val age = FormField.required.integer("age")
  }

  val form = Body.webForm(fields.name, fields.age)

  val FIELD_MESSAGES = Map(
    AgeEntry.fields.name -> "select the user name",
    AgeEntry.fields.age -> "this should be a number")

  def apply(): AgeEntry = apply(WebForm(Form(), Nil))

  def apply(webForm: WebForm): AgeEntry = new AgeEntry(
    webForm.form.fields.mapValues(_.mkString(",")),
    Map(webForm.errors.map(ip => ip.param.name -> ip.reason).toSeq: _*)
  )
}

case class AgeEntry(values: Map[String, String], errors: Map[String, String]) extends View
