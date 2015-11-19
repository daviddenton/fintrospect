package examples.full.test.env

object RunnableEnvironment extends App {
  val userDirectoryPort = 10000
  val entryLoggerPort = 10001

  new TestEnvironment(userDirectoryPort, entryLoggerPort).start()
  Thread.currentThread().join()
}
