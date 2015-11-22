package examples.full.test.env

import com.twitter.util.Await
import org.scalatest.{BeforeAndAfterEach, Suite}

trait RunningTestEnvironment extends BeforeAndAfterEach {
  self: Suite =>

  lazy val serverPort = 9999
  lazy val userDirectoryPort = 10000
  lazy val entryLoggerPort = 10001

  val env = new TestEnvironment(serverPort, userDirectoryPort, entryLoggerPort)

  override protected def beforeEach() = {
    Await.result(env.start())
  }

  override protected def afterEach() = {
    Await.result(env.stop())
  }
}
