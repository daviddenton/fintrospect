package examples.formvalidation

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Request
import io.fintrospect.parameters.StringValidation.EmptyIsInvalid
import io.fintrospect.parameters.{Body, Form, FormField, ParameterSpec, WebForm}
import io.fintrospect.templating.View
import io.fintrospect.templating.View.viewToFuture
import io.fintrospect.util.{Validated, ValidationFailed}
import io.fintrospect.{RouteSpec, ServerRoutes}

import scala.language.reflectiveCalls

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
      postedForm.validate() match {
        case Validated(form) => DisplayUserAge.tupled(postedForm.form <-- (NameAndAgeForm.fields.name, NameAndAgeForm.fields.age))
        case ValidationFailed(errors) => NameAndAgeForm(NAMES, postedForm)
      }
    }
  }

  // provides form validation on POST to same route
  add(RouteSpec().body(NameAndAgeForm.form).at(Post) bindTo submit)
}

case class Name private(value: String)

object Name {
  def validate(value: String) = {
    assert(value.charAt(0).isUpper)
    Name(value)
  }
}

case class Age private(value: Int)

object Age {
  def validate(value: Int) = {
    assert(value >= 18)
    Age(value)
  }
}


case class DisplayUserAge(name: Name, age: Age) extends View

object NameAndAgeForm {

  object fields {
    val name = FormField.required(ParameterSpec.string("name", validation = EmptyIsInvalid).map(Name.validate))
    val age = FormField.required(ParameterSpec.int("age").map(Age.validate))
  }

  val form = Body.webForm(
    fields.name -> "Names must start with capital letter",
    fields.age -> "Must be an adult")

  def apply(names: Seq[String], webForm: WebForm = WebForm(Form(), Nil)): NameAndAgeForm = {
    new NameAndAgeForm(names,
      webForm.form.fields.mapValues(_.mkString(",")),
      Map(webForm.errors.map(ip => ip.param.name -> ip.reason): _*)
    )
  }
}

case class NameAndAgeForm(names: Seq[String], values: Map[String, String], errors: Map[String, String]) extends View
