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


abstract class AutoFiltersSpec[J](val f: AutoFilters[J]) extends FunSpec with Matchers {
  val aLetter = Letter(StreetAddress("my house"), StreetAddress("your house"), "hi there")

  private def request = {
    val request = Request()
    request.content = toBuf(aLetter)
    request
  }

  def toBuf(l: Letter): Buf

  def toOut(): f.AsOut[Letter, J]

  def fromBuf(s: Buf): Letter

  def bodySpec: BodySpec[Letter]

  describe("Auto filters") {

    describe("AutoIn") {
      val svc = f.AutoIn(Body(bodySpec)).andThen(Service.mk { in: Letter => {
        val r = Response()
        r.content = toBuf(in)
        Future(r)
      }
      })

      it("takes the object from the request") {
        fromBuf(result(svc(request)).content) shouldBe aLetter
      }

      it("rejects illegal content with a BadRequest") {
        val request = Request()
        request.contentString = "invalid"
        Await.result(svc(request)).status shouldBe Status.BadRequest
      }
    }

    describe("AutoOut") {
      it("takes the object from the request") {
        val svc = f.AutoOut[Letter](Status.Created)(toOut()).andThen(Service.mk { in: Request => Future.value(aLetter) })
        val response = result(svc(Request()))
        response.status shouldBe Status.Created
        fromBuf(response.content) shouldBe aLetter
      }
    }

    describe("AutoInOut") {
      it("returns Ok") {
        val svc = f.AutoInOut[Letter, Letter](Body(bodySpec), Status.Created)(toOut())
          .andThen(Service.mk { in: Letter => Future.value(in) })
        val response = result(svc(request))
        response.status shouldBe Status.Created
        fromBuf(response.content) shouldBe aLetter
      }
    }

    describe("AutoInOptionalOut") {
      val filter = f.AutoInOptionalOut(Body(bodySpec))(toOut())
      it("returns Ok when present") {
        val svc = filter.andThen(Service.mk[Letter, Option[Letter]] { in => Future.value(Option(in)) })
        val response = result(svc(request))
        response.status shouldBe Status.Ok
        fromBuf(response.content) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = filter.andThen(Service.mk[Letter, Option[Letter]] { in => Future.value(None) })
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

    describe("AutoOptionalOut") {
      val filter = f.AutoOptionalOut[Request, Letter](Status.Created)(toOut())
      it("returns Ok when present") {
        val svc = filter.andThen(Service.mk[Request, Option[Letter]] { in => Future.value(Option(aLetter)) })

        val response = result(svc(Request()))
        response.status shouldBe Status.Created
        fromBuf(response.content) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = filter.andThen(Service.mk[Request, Option[Letter]] { _ => Future.value(None) })
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

  }

}
