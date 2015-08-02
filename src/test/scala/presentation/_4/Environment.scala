package presentation._4

import presentation.Books

object Environment extends App {
  new FakeRemoteLibrary(new Books)
  new SearchApp
  Thread.currentThread().join()
}

/**
 * showcase: remote api working, pass-through
 */