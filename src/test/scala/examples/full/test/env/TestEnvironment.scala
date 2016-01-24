package examples.full.test.env

import java.time.{Clock, Instant, ZoneId}

import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future}
import examples.full.main.SecuritySystem
import io.fintrospect.testing.TestHttpServer

class TestEnvironment(serverPort: Int, userDirectoryPort: Int, entryLoggerPort: Int) {

  val clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())
  val userDirectory = new FakeUserDirectoryState()
  val entryLogger = new FakeEntryLoggerState()

  val userDirectoryServer = new TestHttpServer(userDirectoryPort, userDirectory)
  val entryLoggerServer = new TestHttpServer(entryLoggerPort, entryLogger)

  private val securitySystem = new SecuritySystem(serverPort, userDirectoryPort, entryLoggerPort, clock)

  def responseTo(request: Request) = {
    val msg = Await.result(Http.newService(s"localhost:$serverPort")(request))
    ResponseStatusAndContent(msg.status, msg.headerMap.getOrElse("Content-type", null), msg.contentString)
  }

  def start() = {
    userDirectory.reset()
    entryLogger.reset()

    Future.collect(Seq(
      userDirectoryServer.start(),
      entryLoggerServer.start(),
      securitySystem.start()))
  }

  def stop() = Future.collect(Seq(
    securitySystem.stop(),
    userDirectoryServer.stop(),
    entryLoggerServer.stop()))
}
