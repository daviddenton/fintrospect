package examples.full.test.feature

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status._
import examples.full.test.env.{ResponseStatusAndContent, RunningTestEnvironment}
import io.fintrospect.formats.json.Json4s.Native.JsonFormat
import org.json4s.JString
import org.scalatest.{FunSpec, ShouldMatchers}

class NonFunctionalRequirementsTest extends FunSpec with ShouldMatchers with RunningTestEnvironment {

  it("responds to ping") {
    env.responseTo(Request("/internal/ping")) shouldBe ResponseStatusAndContent(Ok, "pong")
  }

  it("provides API documentation in swagger 2.0 format") {
    val response = env.responseTo(Request("/security/api-docs"))
    response.status shouldBe Ok

    JsonFormat.parse(response.content).children.head.asInstanceOf[JString].values shouldBe "2.0"
  }
}
