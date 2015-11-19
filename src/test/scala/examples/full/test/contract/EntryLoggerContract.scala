package examples.full.test.contract

import java.time.{Clock, Instant, ZoneId}

import com.twitter.util.Await
import examples.full.main._
import org.scalatest.{FunSpec, ShouldMatchers}

/**
 * This represents the contract that both the real and fake EntryLogger servers will adhere to.
 */
trait EntryLoggerContract extends FunSpec with ShouldMatchers {
  def authority: String

  private val time = Instant.now()
  val entryLogger = new EntryLogger(authority, Clock.fixed(time, ZoneId.systemDefault()))

  it("can log a user entry and it is listed") {
    Await.result(entryLogger.enter(User(Id(1), Username("bob"), EmailAddress("bob@bob.com")))) shouldBe UserEntry("bob", goingIn = true, time.toEpochMilli)
    Await.result(entryLogger.exit(User(Id(1), Username("bob"), EmailAddress("bob@bob.com")))) shouldBe UserEntry("bob", goingIn = false, time.toEpochMilli)

    Await.result(entryLogger.list()) shouldBe Seq(
      UserEntry("bob", goingIn = true, time.toEpochMilli),
      UserEntry("bob", goingIn = false, time.toEpochMilli)
    )
  }
}


