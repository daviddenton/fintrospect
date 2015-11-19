package examples.full.test.env

import com.twitter.util.Await
import org.scalatest.{BeforeAndAfter, Suite}

trait RunningTestEnvironment extends BeforeAndAfter {
  self: Suite =>

  lazy val serverPort = 9999
  lazy val userDirectoryPort = 10000
  lazy val entryLoggerPort = 10001

  var env = new TestEnvironment(serverPort, userDirectoryPort, entryLoggerPort)

  before {
    Await.result(env.start())
  }

  after {
    Await.result(env.stop())
  }
}
