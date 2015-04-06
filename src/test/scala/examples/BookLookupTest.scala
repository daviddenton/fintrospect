package examples

import com.twitter.finagle.http.Request
import io.github.daviddenton.fintrospect.testing.TestingFintrospectRoute
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

/*
  an simple example of how to test a Fintrospect Route with the TestingFintrospectRoute trait
 */
class BookLookupTest extends FunSpec with ShouldMatchers with TestingFintrospectRoute {

  override val route = new BookLookup(new Books())

  describe("Book Lookup") {
    it("can lookup an existing book") {
      parse(responseFor(Request("/book/hp1")).contentString) should be === Book("hairy porker", "j.k oinking", 799).toJson
    }
  }

  it("non-existing book") {
    responseFor(Request("/book/hp8")).getStatusCode() should be === 404
  }
}
