package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Request
import io.fintrospect.parameters.InvalidParameter.Missing
import org.scalatest._

class CompositeTest extends FunSpec with ShouldMatchers {

  case class Example(a: Option[String], b: Int)

  describe("Composite") {

    it("successfully extracts when all parameters present") {
      val c = Composite {
        request: Request =>
          for {
            str <- Query.optional.string("name").validate(request)
            int <- Query.required.int("name2").validate(request)
          } yield Example(str, int)
      }

      c <--? Request("/?name=query1&name2=12") shouldBe Extracted(Example(Some("query1"), 12))
    }

    it("reports when not all parameters present") {
      val int = Query.required.int("name2")
      val c = Composite {
        request: Request => for {
          name <- Query.optional.string("name").validate(request)
          name2 <- int.validate(request)
        } yield Example(name, name2)
      }

      c <--? Request("/?name=query1") shouldBe ExtractionFailed(Missing(int))
    }

    it("validation error between parameters") {

      case class Range(startDate: LocalDate, endDate: LocalDate)

      val start = Query.optional.localDate("start")
      val middle = Query.optional.localDate("middle")
      val end = Query.required.localDate("end")

      val c = Composite {
        request: Request => {
          for {
            startDate <- start.validate(request)
            middleDate <- middle.validate(request, "not after start", _.get.isAfter(startDate.get))
            endDate <- end.validate(request, "not after middle", _.isAfter(middleDate.get))
          } yield Range(startDate.get, endDate)
        }
      }

      c <--? Request("/?start=2001-01-01&middle=2000-01-01&end=2002-01-01") shouldBe ExtractionFailed(InvalidParameter(middle, "not after start"))
    }
  }

}
