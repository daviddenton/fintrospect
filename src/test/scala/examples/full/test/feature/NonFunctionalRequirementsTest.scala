package examples.full.test.feature

import com.twitter.finagle.http.{Request, Status}
import examples.full.test.env.RunningTestEnvironment
import org.scalatest.{FunSpec, ShouldMatchers}

class NonFunctionalRequirementsTest extends FunSpec with ShouldMatchers with RunningTestEnvironment {

  it("responds to ping") {
    env.responseTo(Request("/internal/ping")) shouldBe (Status.Ok, "pong")
  }
}
