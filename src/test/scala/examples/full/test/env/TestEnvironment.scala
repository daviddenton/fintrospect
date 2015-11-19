package examples.full.test.env

import java.time.{Clock, Instant, ZoneId}

import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future}
import examples.full.main.SecuritySystem
import io.fintrospect.testing.TestHttpServer
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom

class TestEnvironment(serverPort: Int, userDirectoryPort: Int, entryLoggerPort: Int) {

  val clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())
  val userDirectory = new FakeUserDirectoryState()
  val entryLogger = new FakeEntryLoggerState()

  val userDirectoryServer = new TestHttpServer(userDirectoryPort, userDirectory)
  val entryLoggerServer = new TestHttpServer(entryLoggerPort, entryLogger)

  private val securitySystem = new SecuritySystem(serverPort, userDirectoryPort, entryLoggerPort, clock)

  def responseTo(request: Request) = {
    statusAndContentFrom(Await.result(Http.newService(s"localhost:$serverPort")(request)))
  }

  def start() = Future.collect(Seq(
    userDirectoryServer.start(),
    entryLoggerServer.start(),
    securitySystem.start()))

  def stop() = Future.collect(Seq(
    securitySystem.stop(),
    userDirectoryServer.stop(),
    entryLoggerServer.stop()))
}
