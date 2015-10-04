package examples.extended

import com.twitter.finagle.httpx._
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.testing.TestingFintrospectRoute
import io.fintrospect.util.HttpRequestResponseUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

/*
  an simple example of how to test a Fintrospect Route with the TestingFintrospectRoute trait
 */
class BookLookupTest extends FunSpec with ShouldMatchers with TestingFintrospectRoute {

  override val route = new BookLookup(new Books()).route

  describe("Book Lookup") {
    it("can lookup an existing book") {
      parse(contentFrom(responseFor(Request("/book/hp1")))) shouldEqual Book("hairy porker", "j.k oinking", 799).toJson
    }
  }

  it("non-existing book") {
    statusAndContentFrom(responseFor(Request("/book/hp8")))._1 shouldEqual Status.NotFound
  }
}
