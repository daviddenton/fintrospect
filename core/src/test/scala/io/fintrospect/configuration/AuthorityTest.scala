package io.fintrospect.configuration

import java.net.InetSocketAddress

import org.scalatest.{FunSpec, ShouldMatchers}

class AuthorityTest extends FunSpec with ShouldMatchers {

  describe("Authority") {
    it("renders ok") {
      Authority(Host.localhost, Port(9999)).toString shouldEqual "localhost:9999"
    }
    it("defaults no port to port 80") {
      Authority.unapply("localhost") shouldEqual Some(Authority(Host.localhost, Port(80)))
    }
    it("defaults valid host and port") {
      Authority.unapply("localhost:123") shouldEqual Some(Authority(Host.localhost, Port(123)))
    }
    it("invalid port number") {
      Authority.unapply("localhost:asd") shouldEqual None
    }
    it("too many parts") {
      Authority.unapply("localhost:123:123") shouldEqual None
    }
    it("socket address") {
      Authority(Host.localhost, Port(9999)).socketAddress shouldEqual new InetSocketAddress("localhost", 9999)
    }
  }
}
