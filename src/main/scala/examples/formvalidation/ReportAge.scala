package examples.formvalidation

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Request
import io.fintrospect.parameters.ExtractionError.Missing
import io.fintrospect.parameters.StringValidation.EmptyIsInvalid
import io.fintrospect.parameters.{Body, Form, FormField, Validated, ValidationFailed, Validator, WebForm}
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

      postedForm.validate(NameAndAgeForm.rules) match {
        case Validated((theAge, theName)) => DisplayUserAge(theName.get, theAge.get)
        case ValidationFailed(errors) => NameAndAgeForm(NAMES, postedForm.withErrors(errors))
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

  // we can use a validator here to provide extra validation
  def rules(form: Form) = Validator.mk(
    NameAndAgeForm.fields.age <--?(form, "Must be an adult", _ >= 18),
    NameAndAgeForm.fields.name <--?(form, "Must start with Capital letter", _.charAt(0).isUpper)
  )

  def apply(names: Seq[String], webForm: WebForm = WebForm(Form(), Nil)): NameAndAgeForm = {
    val e = webForm.errors.map {
      case Missing(NameAndAgeForm.fields.age.name) => NameAndAgeForm.fields.name.name -> "required"
      case Missing(NameAndAgeForm.fields.name.name) => NameAndAgeForm.fields.name.name -> "select the user name"
      case ip => ip.name -> ip.reason
    }

    new NameAndAgeForm(names, webForm.form.fields.mapValues(_.mkString(",")), Map(e: _*))
  }
}

case class NameAndAgeForm(names: Seq[String], values: Map[String, String], errors: Map[String, String]) extends View
