package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Request
import io.fintrospect.parameters.InvalidParameter.Missing
import org.scalatest._

class ExtractableTest extends FunSpec with ShouldMatchers {

  case class Example(a: Option[String], b: Option[String], c: Int)

  describe("Extractable") {

    it("successfully extracts when all parameters present") {
      val c = Extractable.mk {
        request: Request => {

          val a: Extraction[Example] = for {
            name1 <- Query.optional.string("name1").extract(request)
            name2 <- Query.optional.string("name2").extract(request)
            name3 <- Query.required.int("name3").extract(request)
          } yield Example(name1, name2, name3.get)
          a
        }
      }

      c <--? Request("/?name1=query1&name2=rwer&name3=12") shouldBe Extracted(Example(Some("query1"), Some("rwer"), 12))
    }

    it("reports error when not all parameters present") {
      val int = Query.required.int("name3")
      val c = Extractable.mk {
        request: Request => for {
          name1 <- Query.optional.string("name1").extract(request)
          name2 <- Query.optional.string("name2").extract(request)
          name3 <- int.extract(request)
        } yield Example(name1, name2, name3.get)
      }

      c <--? Request("/?name1=query1") shouldBe ExtractionFailed(Missing(int))
    }

    it("extracted when only optional parameters missing") {

      val int = Query.required.int("name3")
      val c = Extractable.mk {
        request: Request => for {
          name1 <- Query.optional.string("name1").extract(request)
          name2 <- Query.optional.string("name2").extract(request)
          name3 <- int.extract(request)
        } yield Example(name1, name2, name3.get)
      }

      c <--? Request("/?name3=123") shouldBe Extracted(Example(None, None, 123))
    }

    it("validation error between parameters") {

      case class Range(startDate: LocalDate, middleDate: Option[LocalDate], endDate: LocalDate)

      val start = Query.optional.localDate("start")
      val middle = Query.optional.localDate("middle")
      val end = Query.required.localDate("end")

      val c = Extractable.mk {
        request: Request => {
          for {
            startDate <- start <--? request
            middleDate <- middle <--?(request, "not after start", (i: LocalDate) => i.isAfter(startDate.get))
            endDate <- end <--?(request, "not after start", e => startDate.map(s => e.isAfter(s)).getOrElse(true))
          } yield Range(startDate.get, middleDate, endDate.get)
        }
      }

      c <--? Request("/?start=2002-01-01&end=2001-01-01") shouldBe ExtractionFailed(InvalidParameter(end, "not after start"))
    }
  }

}
