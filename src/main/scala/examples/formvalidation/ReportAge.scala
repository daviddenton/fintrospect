package examples.formvalidation

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Request
import io.fintrospect.parameters.StringValidation.EmptyIsInvalid
import io.fintrospect.parameters.Validator.Opt
import io.fintrospect.parameters.{Body, Form, FormField, Parameter, Validated, Validation, ValidationFailed, Validator, WebForm}
import io.fintrospect.templating.View
import io.fintrospect.templating.View.viewToFuture
import io.fintrospect.{RouteSpec, ServerRoutes}

/**
  * This is a set of 2 routes which model:
  * 1. GET route - form display
  * 2. POST route - submission of form
  */
class ReportAge extends ServerRoutes[Request, View] {

  private val NAMES = Seq("Bob", "Johnny", "Rita", "Sue")

  // displays the initial form to the user
  add(RouteSpec().at(Get) bindTo Service.mk { rq: Request => NameAndAgeForm(NAMES) })

  private val submit = Service.mk {
    rq: Request => {
      val postedForm = NameAndAgeForm.form <-- rq
      if (postedForm.isValid)
        doFieldValidationOn(postedForm) match {
          case Validated(nextView) => nextView
          case ValidationFailed(errors) => NameAndAgeForm(NAMES, postedForm.copy(errors = errors))
        }
      else NameAndAgeForm(NAMES, postedForm)
    }
  }

  // we can use a validator here to provide extra validation
  private def doFieldValidationOn(submitted: WebForm): Validation[DisplayUserAge] =
    NameAndAgeForm.validate(submitted) {
      case (theAge, theName) => DisplayUserAge(theName.get, theAge.get)
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

  def validate(submitted: WebForm) = {
    Validator.mk(
      NameAndAgeForm.fields.age <--?(submitted.form, "Must be an adult", _ >= 18),
      NameAndAgeForm.fields.name <--?(submitted.form, "Must start with Capital letter", _.charAt(0).isUpper)
    )
  }

  private val FIELD_MESSAGES: Map[Parameter, String] = Map(
    NameAndAgeForm.fields.name -> "select the user name",
    NameAndAgeForm.fields.age -> "this should be a number")

  def apply(names: Seq[String], webForm: WebForm = WebForm(Form(), Nil)): NameAndAgeForm = new NameAndAgeForm(names,
    webForm.form.fields.mapValues(_.mkString(",")),
    Map(webForm.errors.map(ip => ip.param.name -> FIELD_MESSAGES(ip.param)).toSeq: _*)
  )
}

case class NameAndAgeForm(names: Seq[String], values: Map[String, String], errors: Map[String, String]) extends View
