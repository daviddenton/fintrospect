package examples.full.test.feature

import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status._
import examples.full.main._
import examples.full.test.env.{ResponseStatusAndContent, RunningTestEnvironment}
import org.scalatest.{FunSpec, ShouldMatchers}

class EnteringAndExitingTest extends FunSpec with ShouldMatchers with RunningTestEnvironment {

  describe("when a user is unknown") {
    it("does not allow user in") {
      enterBuilding(Option("Rita"), "realSecret").status shouldBe NotFound
    }
  }

  describe("entry endpoint") {
    it("rejects missing username in entry endpoint") {
      enterBuilding(None, "realSecret").status shouldBe BadRequest
    }

    it("is protected with a secret key") {
      enterBuilding(Option("Bob"), "fakeSecret").status shouldBe Unauthorized
    }
  }

  describe("exit endpoint") {
    it("rejects missing username in exit endpoint") {
      exitBuilding(None, "fakeSecret").status shouldBe BadRequest
    }

    it("is protected with a secret key") {
      exitBuilding(Option("Bob"), "fakeSecret").status shouldBe Unauthorized
    }
  }

  describe("when a user is not in the building") {
    it("does not allow user to exit") {
      exitBuilding(Option("Bob"), "realSecret").status shouldBe BadRequest
    }
  }

  describe("when a known user tries to enter") {
    it("allows the user in and logs entry") {
      env.userDirectory.contains(User(Id(1), Username("Bob"), EmailAddress("bob@bob.com")))
      enterBuilding(Option("Bob"), "realSecret").status shouldBe Accepted
      env.entryLogger.entries shouldBe Seq(UserEntry("Bob", goingIn = true, env.clock.millis()))
    }

    it("does not allow user to enter once inside") {
      env.userDirectory.contains(User(Id(1), Username("Bob"), EmailAddress("bob@bob.com")))
      enterBuilding(Option("Bob"), "realSecret")
      enterBuilding(Option("Bob"), "realSecret").status shouldBe BadRequest
    }

    it("allows the user to exit and logs exit") {
      env.userDirectory.contains(User(Id(1), Username("Bob"), EmailAddress("bob@bob.com")))
      enterBuilding(Option("Bob"), "realSecret")
      exitBuilding(Option("Bob"), "realSecret").status shouldBe Accepted

      env.entryLogger.entries shouldBe Seq(
        UserEntry("Bob", goingIn = false, env.clock.millis())
      )
    }
  }

  private def enterBuilding(user: Option[String], secret: String): ResponseStatusAndContent = {
    val query = user.map("username=" + _).getOrElse("")
    val request = Request(Post, "/security/knock?" + query)
    request.headerMap("key") = secret
    env.responseTo(request)
  }

  private def exitBuilding(user: Option[String], secret: String): ResponseStatusAndContent = {
    val query = user.map("username=" + _).getOrElse("")
    val request = Request(Post, "/security/bye?" + query)
    request.headerMap("key") = secret
    env.responseTo(request)
  }
}
