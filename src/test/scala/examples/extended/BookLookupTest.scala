package examples.extended

import com.twitter.finagle.http.Request
import com.twitter.io.Charsets
import io.fintrospect.testing.TestingFintrospectRoute
import io.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.scalatest.{FunSpec, ShouldMatchers}

/*
  an simple example of how to test a Fintrospect Route with the TestingFintrospectRoute trait
 */
class BookLookupTest extends FunSpec with ShouldMatchers with TestingFintrospectRoute {

  override val route = new BookLookup(new Books()).route

  describe("Book Lookup") {
    it("can lookup an existing book") {
      parse(responseFor(Request("/book/hp1")).getContent.toString(Charsets.Utf8)) shouldEqual Book("hairy porker", "j.k oinking", 799).toJson
    }
  }

  it("non-existing book") {
    responseFor(Request("/book/hp8")).getStatus shouldEqual HttpResponseStatus.NOT_FOUND
  }
}
