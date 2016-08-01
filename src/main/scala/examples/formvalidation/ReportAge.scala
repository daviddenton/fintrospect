package examples.formvalidation

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Request
import io.fintrospect.parameters.{Body, Form, FormField, ParameterSpec}
import io.fintrospect.templating.View
import io.fintrospect.util.EitherF.eitherF
import io.fintrospect.{RouteSpec, ServerRoutes}

import scala.language.reflectiveCalls

/**
  * This is a set of 2 routes which model:
  * 1. GET route - form display
  * 2. POST route - submission of form
  */
class ReportAge(greetingDatabase: GreetingDatabase) extends ServerRoutes[Request, View] {

  object Validations {
    def checkAge(age: Age): Boolean = true

    def checkName(name: Name): Boolean = true
  }

  private val NAMES = Seq("Bob", "Johnny", "Rita", "Sue")

  // displays the initial form to the user
  add(RouteSpec().at(Get) bindTo Service.mk { rq: Request => NameAndAgeForm(NAMES, Form(), None) })

  private val submitAndValidate = Service.mk {
    rq: Request => {
      val postedForm = NameAndAgeForm.form <-- rq

      // the eitherF construct is a right-biased wrapper for an Either which also supports the
      // Future-based operations that finagle uses.
      eitherF {
        if (postedForm.isValid) Right(postedForm) else Left("form has errors")
      }.flatMap {
        form => Right((NameAndAgeForm.fields.age <-- form, NameAndAgeForm.fields.name <-- form))
      }.flatMapF {
        case (age, name) =>
          greetingDatabase
            .lookupGreeting(age, name)
            .map(greeting => greeting
              .map(g => Right((age, name, g)))
              .getOrElse(Left("No idea how to greet you")))
      } matchF {
        case Right((age, name, greeting)) => DisplayUserAge(greeting, name, age)
        case Left(error) => NameAndAgeForm(NAMES, postedForm, Option(error))
      }
    }
  }

  // provides form validation on POST to same route
  add(RouteSpec().body(NameAndAgeForm.form).at(Post) bindTo submitAndValidate)
}

// Form fields classes - these encapsulate the validation logic. We can use "asserts" to define the field
case class Name private(value: String)

object Name {
  private def validate(value: String) = {
    assert(value.length > 0 && value.charAt(0).isUpper)
    Name(value)
  }

  val specAndMessage = ParameterSpec.string("name").map(Name.validate) -> "Names must start with capital letter"
}

case class Age private(value: Int)

object Age {
  private def validate(value: Int) = {
    assert(value >= 18)
    Age(value)
  }

  val specAndMessage = ParameterSpec.int("age").map(Age.validate) -> "Must be an adult"
}


// this is the "Form" view", defining maps of the current state of the form and errors for each named field
case class NameAndAgeForm(names: Seq[String], values: Map[String, String], errors: Map[String, String]) extends View

object NameAndAgeForm {

  object fields {
    val name = FormField.required(Name.specAndMessage._1)
    val age = FormField.required(Age.specAndMessage._1)
  }

  val form = Body.webForm(
    fields.name -> Name.specAndMessage._2,
    fields.age -> Age.specAndMessage._2)

  def apply(names: Seq[String], webForm: Form, generalError: Option[String]): NameAndAgeForm = {
    val formErrors = webForm.errors.map(ip => ip.param.name -> ip.reason)
    val allErrors = generalError.map(message => "general" -> message).toSeq ++ formErrors
    new NameAndAgeForm(names,
      webForm.fields.mapValues(_.mkString(",")),
      Map(allErrors: _*)
    )
  }
}

// finally, this is the "success" view, which is displayed when the
case class DisplayUserAge(greeting: String, name: Name, age: Age) extends View

