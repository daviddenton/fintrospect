package examples.full.test.env

import java.time.{Clock, Instant, ZoneId}

import com.twitter.util.Future
import io.fintrospect.testing.TestHttpServer

class TestEnvironment(userDirectoryPort: Int, entryLoggerPort: Int) {

  val clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())

  val userDirectory = new FakeUserDirectoryState()
  private val userDirectoryServer = new TestHttpServer(userDirectoryPort, userDirectory)

  val entryLogger = new FakeEntryLoggerState()
  private val entryLoggerServer = new TestHttpServer(entryLoggerPort, entryLogger)

  def start() = Future.collect(Seq(userDirectoryServer.start(), entryLoggerServer.start()))

  def stop() = Future.collect(Seq(userDirectoryServer.stop(), entryLoggerServer.stop()))
}
