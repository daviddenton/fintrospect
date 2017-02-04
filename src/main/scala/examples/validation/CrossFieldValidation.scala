package examples.validation

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.util.Await.result
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.parameters.Query
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import io.fintrospect.util.{Extracted, ExtractionFailed, Extractor}
import io.fintrospect.{RouteModule, RouteSpec}

case class Person(gender: Option[String], experience: Int)

case class SchoolClass(pupils: Int, teacher: Person)

/**
  * This example shows how to apply cross-field validation rules for the request using a for comprehensions
  * and the "Extraction" construct. The comprehension returns an Extracted, or ExtractionFailed instance.
  * Note that the Extractables can be nested in other Extractables, which allows for re-use and compartmentalisation of
  * validation logic.
  *
  * In this example, we implement a checker for school class sizes.. the rule being that the number of pupils in a class
  * must be greater than their teacher's years of experience.
  */
object CrossFieldValidation extends App {
  type Predicate[T] = T => Boolean

  // lower level extractor: extracts a person from the request
  val person: Extractor[Request, Person] = Extractor.mk {
    req: Request =>
      for {
        gender <- Query.optional.string("gender") <--? req
        exp <- Query.required.int("experience") <--? req
      } yield Person(gender, exp)
  }

  // higher-level extractor: uses other extractors and validation rules
  val acceptableClassSize: Extractor[Request, SchoolClass] = {

    // this is a cross-field validation rule, which is basically a predicate and a reason for failure
    def lessThanYearsExperience(teacher: Person): Predicate[Int] = number => teacher.experience > number

    Extractor.mk {
      req: Request =>
        for {
          teacher <- person <--? req
          pupils <- Query.required.int("pupils") <--? (req, "Too many pupils", lessThanYearsExperience(teacher))
        } yield {
          SchoolClass(pupils, teacher)
        }
    }
  }

  // HTTP route which applies the validation - returning the overall Extraction result in case of success
  val checkClassSize = RouteSpec().at(Get) bindTo Service.mk {
    req: Request => {
      acceptableClassSize <--? req match {
        case Extracted(clazz) => Ok(clazz.toString)
        case ExtractionFailed(sp) => BadRequest(sp.mkString(", "))
      }
    }
  }

  val svc = RouteModule(Root).withRoute(checkClassSize).toService

  // print succeeding and failing cases
  println("Missing parameters case: " + statusAndContentFrom(result(svc(Request("?gender=male&pupils=10")))))
  println("Failing logic case: " + statusAndContentFrom(result(svc(Request("?gender=male&experience=9&pupils=10")))))
  println("Successful case: " + statusAndContentFrom(result(svc(Request("?gender=female&experience=16&pupils=15")))))

}
