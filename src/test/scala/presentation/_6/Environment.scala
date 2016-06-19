package presentation._6

import presentation.Books

object Environment extends App {
  new FakeRemoteLibrary(new Books)
  new SearchApp
  Thread.currentThread().join()
}

/**
 * showcase: swagger schema
 */