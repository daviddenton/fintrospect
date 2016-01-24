package examples.full.test.feature

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status._
import examples.full.test.env.RunningTestEnvironment
import io.fintrospect.ContentTypes
import org.scalatest.{FunSpec, ShouldMatchers}

class WebTest extends FunSpec with ShouldMatchers with RunningTestEnvironment {

  it("homepage") {
    val response = env.responseTo(Request("/"))
    response.status shouldBe Ok
    response.contentType.startsWith(ContentTypes.TEXT_HTML.value) shouldBe true
  }

  it("known users") {
    val response = env.responseTo(Request("/"))
    response.status shouldBe Ok
    response.contentType.startsWith(ContentTypes.TEXT_HTML.value) shouldBe true
  }
}
