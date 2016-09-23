package examples.extended

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.NotFound
import io.fintrospect.formats.Argo.JsonFormat.parse
import io.fintrospect.testing.TestingFintrospectRoute
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import org.scalatest.{FunSpec, Matchers}

/*
  an simple example of how to test a Fintrospect Route with the TestingFintrospectRoute trait
 */
class BookLookupTest extends FunSpec with Matchers with TestingFintrospectRoute {

  override val route = new BookLookup(new Books()).route

  describe("Book Lookup") {
    it("can lookup an existing book") {
      parse(responseFor(Request("/book/hp1")).contentString) shouldBe Book("hairy porker", "j.k oinking", 799).toJson
    }
  }

  it("non-existing book") {
    statusAndContentFrom(responseFor(Request("/book/hp8")))._1 shouldBe NotFound
  }
}
