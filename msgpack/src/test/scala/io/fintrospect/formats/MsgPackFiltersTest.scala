package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.Created
import com.twitter.finagle.http.{Request, Status}
import com.twitter.io.Buf.ByteArray.Shared.extract
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.formats.MsgPack.ResponseBuilder.implicits._
import org.scalatest.{FunSpec, Matchers}

import scala.language.reflectiveCalls

class MsgPackFiltersTest extends FunSpec with Matchers {

  describe("MsgPack.Filters") {
    val aLetter = MsgPackLetter(MsgPackStreetAddress("my house"), MsgPackStreetAddress("your house"), "hi there")

    val request = Request()
    request.content = MsgPackMsg(aLetter).toBuf

    describe("AutoInOut") {
      it("returns Ok") {
        val svc = MsgPack.Filters.AutoInOut(Service.mk { in: MsgPackLetter => Future.value(in) }, Created)

        val response = result(svc(request))
        new MsgPackMsg(extract(response.content)).as[MsgPackLetter] shouldBe aLetter
      }
    }

    describe("AutoInOptionalOut") {
      it("returns Ok when present") {
        val svc = MsgPack.Filters.AutoInOptionalOut(Service.mk[MsgPackLetter, Option[MsgPackLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(request))
        response.status shouldBe Status.Ok
        new MsgPackMsg(extract(response.content)).as[MsgPackLetter] shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = MsgPack.Filters.AutoInOptionalOut(Service.mk[MsgPackLetter, Option[MsgPackLetter]] { in => Future.value(None) })
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

    describe("AutoIn") {
      val svc = MsgPack.Filters.AutoIn(MsgPack.Format.body[MsgPackLetter]()).andThen(Service.mk { in: MsgPackLetter => Status.Ok(MsgPackMsg(in)) })
      it("takes the object from the request") {
        MsgPack.Format.decode[MsgPackLetter](result(svc(request)).content) shouldBe aLetter
      }

      it("rejects illegal content with a BadRequest") {
        val request = Request()
        request.contentString = "not msgpack"
        result(svc(request)).status shouldBe Status.BadRequest
      }

    }

    describe("AutoOut") {
      it("takes the object from the request") {
        val svc = MsgPack.Filters.AutoOut[MsgPackLetter, MsgPackLetter](Created).andThen(Service.mk { in: MsgPackLetter => Future.value(in) })
        val response = result(svc(aLetter))
        response.status shouldBe Created
        new MsgPackMsg(extract(response.content)).as[MsgPackLetter] shouldBe aLetter
      }
    }

    describe("AutoOptionalOut") {
      it("returns Ok when present") {
        val svc = MsgPack.Filters.AutoOptionalOut[MsgPackLetter, MsgPackLetter](Created).andThen(Service.mk[MsgPackLetter, Option[MsgPackLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(aLetter))
        response.status shouldBe Created
        new MsgPackMsg(extract(response.content)).as[MsgPackLetter] shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = MsgPack.Filters.AutoOptionalOut[MsgPackLetter, MsgPackLetter](Created).andThen(Service.mk[MsgPackLetter, Option[MsgPackLetter]] { in => Future.value(None) })
        result(svc(aLetter)).status shouldBe Status.NotFound
      }
    }
  }
}
