package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.io.Buf
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.parameters.{Body, BodySpec}
import org.scalatest.{FunSpec, Matchers}

case class StreetAddress(address: String)

case class Letter(to: StreetAddress, from: StreetAddress, message: String)

case class LetterOpt(to: StreetAddress, from: StreetAddress, message: Option[String])


abstract class AutoSpec[J](val f: Auto[J]) extends FunSpec with Matchers {
  val aLetter = Letter(StreetAddress("my house"), StreetAddress("your house"), "hi there")

  private def request = {
    val request = Request()
    request.content = toBuf(aLetter)
    request
  }

  def toBuf(l: Letter): Buf

  def transform(): (Letter => J)

  def fromBuf(s: Buf): Letter

  def bodySpec: BodySpec[Letter]

  describe("Auto filters") {

    describe("In") {
      val svc = f.In(Service.mk { in: Letter => {
        val r = Response()
        r.content = toBuf(in)
        Future(r)
      }
      })(Body(bodySpec, ""))

      it("takes the object from the request") {
        fromBuf(result(svc(request)).content) shouldBe aLetter
      }

      it("rejects illegal content with a BadRequest") {
        val request = Request()
        request.contentString = "invalid"
        Await.result(svc(request)).status shouldBe Status.BadRequest
      }
    }

    describe("Out") {
      it("takes the object from the request") {
        val svc = f.Out(Service.mk { in: Request => Future(aLetter) }, Status.Created)(transform())
        val response = result(svc(Request()))
        response.status shouldBe Status.Created
        fromBuf(response.content) shouldBe aLetter
      }
    }

    describe("InOut") {
      it("returns Ok") {
        val svc = f.InOut(Service.mk { in: Letter => Future(in) }, Status.Created)(Body(bodySpec), transform())
        val response = result(svc(request))
        response.status shouldBe Status.Created
        fromBuf(response.content) shouldBe aLetter
      }
    }

    describe("InOptionalOut") {
      it("returns Ok when present") {
        val svc = f.InOptionalOut(Service.mk[Letter, Option[Letter]] { in => Future(Option(in)) })(Body(bodySpec), transform())
        val response = result(svc(request))
        response.status shouldBe Status.Ok
        fromBuf(response.content) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = f.InOptionalOut(Service.mk[Letter, Option[Letter]] { in => Future(None) })(Body(bodySpec), transform())
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

    describe("OptionalOut") {
      it("returns Ok when present") {
        val svc = f.OptionalOut(Service.mk[Request, Option[Letter]] { in => Future(Option(aLetter)) }, Status.Created)(transform())

        val response = result(svc(Request()))
        response.status shouldBe Status.Created
        fromBuf(response.content) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = f.OptionalOut(Service.mk[Request, Option[Letter]] { _ => Future(None) }, Status.Created)(transform())
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

  }

}
