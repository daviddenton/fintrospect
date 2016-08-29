package io.fintrospect.configuration

import java.net.InetSocketAddress

import org.scalatest.{FunSpec, Matchers}

class AuthorityTest extends FunSpec with Matchers {

  describe("Authority") {
    it("renders ok") {
      Authority(Host.localhost, Port(9999)).toString shouldBe "localhost:9999"
    }
    it("defaults no port to port 80") {
      Authority.unapply("localhost") shouldBe Some(Authority(Host.localhost, Port(80)))
    }
    it("defaults valid host and port") {
      Authority.unapply("localhost:123") shouldBe Some(Authority(Host.localhost, Port(123)))
    }
    it("invalid port number") {
      Authority.unapply("localhost:asd") shouldBe None
    }
    it("too many parts") {
      Authority.unapply("localhost:123:123") shouldBe None
    }
    it("socket address") {
      Authority(Host.localhost, Port(9999)).socketAddress shouldBe new InetSocketAddress("localhost", 9999)
    }
  }
}
