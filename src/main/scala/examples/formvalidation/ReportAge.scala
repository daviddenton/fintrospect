package examples.formvalidation

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Request
import io.fintrospect.parameters.StringValidation.EmptyIsInvalid
import io.fintrospect.parameters.{BodyParameter, Parameter, Body, Form, FormField, WebForm}
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
  add(RouteSpec().at(Get) bindTo Service.mk { rq: Request => NameAndAgeForm() })

  private val submit = Service.mk {
    rq: Request => {
      val submitted = NameAndAgeForm.form <-- rq
      if (submitted.isValid) {
        val name = NameAndAgeForm.fields.name <-- submitted.form
        val age = NameAndAgeForm.fields.age <-- submitted.form
        DisplayUserAge(name, age)
      } else {
        NameAndAgeForm(submitted)
      }
    }
  }

  // provides form validation on POST to same route
  add(RouteSpec().body(NameAndAgeForm.form).at(Post) bindTo submit)
}

case class DisplayUserAge(name: String, age: Int) extends View

object NameAndAgeForm {
  object fields {
    val name = FormField.required.string("name", validation = EmptyIsInvalid)
    val age = FormField.required.integer("age")
  }

  val form = Body.webForm(fields.name, fields.age)

  private val FIELD_MESSAGES: Map[Parameter, String] = Map(
    NameAndAgeForm.fields.name -> "select the user name",
    NameAndAgeForm.fields.age -> "this should be a number")

  def apply(): NameAndAgeForm = apply(WebForm(Form(), Nil))

  def apply(webForm: WebForm): NameAndAgeForm = new NameAndAgeForm(
    webForm.form.fields.mapValues(_.mkString(",")),
    Map(webForm.errors.map(ip => ip.param.name -> FIELD_MESSAGES(ip.param)).toSeq: _*)
  )
}

case class NameAndAgeForm(values: Map[String, String], errors: Map[String, String]) extends View
