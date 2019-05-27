package io.fintrospect.formats

import java.io.OutputStream
import java.nio.charset.Charset.defaultCharset

import com.twitter.finagle.http.Status
import com.twitter.io.{Buf, Bufs}
import io.fintrospect.ContentType
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import io.netty.buffer.Unpooled.copiedBuffer
import org.scalatest.{FunSpec, Matchers}

object LengthyIntResponseBuilder extends AbstractResponseBuilder[Int] {
  override def HttpResponse(): ResponseBuilder[Int] = new ResponseBuilder[Int](i => Buf.Utf8(i.toString), _.length, _.getMessage.length, ContentType("numbers"))
}

class AbstractResponseBuilderSpec extends FunSpec with Matchers {

  val builder = LengthyIntResponseBuilder
  val message = "some text goes here"

  describe("abstract response builder") {

    it("status builders") {
      def checkError(b: ResponseBuilder[Int], s: Status) = {
        statusAndContentFrom(b) shouldBe(s, "5")
      }

      def checkBuilder(b: ResponseBuilder[Int], s: Status) = {
        statusAndContentFrom(b) shouldBe(s, "hello")
      }

      checkBuilder(builder.Continue("hello"), Status.Continue)
      checkBuilder(builder.SwitchingProtocols("hello"), Status.SwitchingProtocols)
      checkBuilder(builder.Processing("hello"), Status.Processing)
      checkBuilder(builder.Ok("hello"), Status.Ok)
      checkBuilder(builder.Created("hello"), Status.Created)
      checkBuilder(builder.Accepted("hello"), Status.Accepted)
      checkBuilder(builder.NonAuthoritativeInformation("hello"), Status.NonAuthoritativeInformation)
      checkBuilder(builder.NoContent("hello"), Status.NoContent)
      checkBuilder(builder.ResetContent("hello"), Status.ResetContent)
      checkBuilder(builder.PartialContent("hello"), Status.PartialContent)
      checkBuilder(builder.MultiStatus("hello"), Status.MultiStatus)
      checkBuilder(builder.MultipleChoices("hello"), Status.MultipleChoices)
      checkBuilder(builder.MovedPermanently("hello"), Status.MovedPermanently)
      checkBuilder(builder.Found("hello"), Status.Found)
      checkBuilder(builder.SeeOther("hello"), Status.SeeOther)
      checkBuilder(builder.NotModified("hello"), Status.NotModified)
      checkBuilder(builder.UseProxy("hello"), Status.UseProxy)
      checkBuilder(builder.TemporaryRedirect("hello"), Status.TemporaryRedirect)
      checkError(builder.BadRequest("hello"), Status.BadRequest)
      checkError(builder.Unauthorized("hello"), Status.Unauthorized)
      checkError(builder.PaymentRequired("hello"), Status.PaymentRequired)
      checkError(builder.Forbidden("hello"), Status.Forbidden)
      checkError(builder.NotFound("hello"), Status.NotFound)
      checkError(builder.MethodNotAllowed("hello"), Status.MethodNotAllowed)
      checkError(builder.NotAcceptable("hello"), Status.NotAcceptable)
      checkError(builder.ProxyAuthenticationRequired("hello"), Status.ProxyAuthenticationRequired)
      checkError(builder.RequestTimeout("hello"), Status.RequestTimeout)
      checkError(builder.Conflict("hello"), Status.Conflict)
      checkError(builder.Gone("hello"), Status.Gone)
      checkError(builder.LengthRequired("hello"), Status.LengthRequired)
      checkError(builder.PreconditionFailed("hello"), Status.PreconditionFailed)
      checkError(builder.RequestEntityTooLarge("hello"), Status.RequestEntityTooLarge)
      checkError(builder.RequestURITooLong("hello"), Status.RequestURITooLong)
      checkError(builder.UnsupportedMediaType("hello"), Status.UnsupportedMediaType)
      checkError(builder.RequestedRangeNotSatisfiable("hello"), Status.RequestedRangeNotSatisfiable)
      checkError(builder.ExpectationFailed("hello"), Status.ExpectationFailed)
      checkError(builder.EnhanceYourCalm("hello"), Status.EnhanceYourCalm)
      checkError(builder.UnprocessableEntity("hello"), Status.UnprocessableEntity)
      checkError(builder.Locked("hello"), Status.Locked)
      checkError(builder.FailedDependency("hello"), Status.FailedDependency)
      checkError(builder.UnorderedCollection("hello"), Status.UnorderedCollection)
      checkError(builder.UpgradeRequired("hello"), Status.UpgradeRequired)
      checkError(builder.PreconditionRequired("hello"), Status.PreconditionRequired)
      checkError(builder.TooManyRequests("hello"), Status.TooManyRequests)
      checkError(builder.RequestHeaderFieldsTooLarge("hello"), Status.RequestHeaderFieldsTooLarge)
      checkError(builder.UnavailableForLegalReasons("hello"), Status.UnavailableForLegalReasons)
      checkError(builder.ClientClosedRequest("hello"), Status.ClientClosedRequest)
      checkError(builder.InternalServerError("hello"), Status.InternalServerError)
      checkError(builder.NotImplemented("hello"), Status.NotImplemented)
      checkError(builder.BadGateway("hello"), Status.BadGateway)
      checkError(builder.ServiceUnavailable("hello"), Status.ServiceUnavailable)
      checkError(builder.GatewayTimeout("hello"), Status.GatewayTimeout)
      checkError(builder.HttpVersionNotSupported("hello"), Status.HttpVersionNotSupported)
      checkError(builder.VariantAlsoNegotiates("hello"), Status.VariantAlsoNegotiates)
      checkError(builder.InsufficientStorage("hello"), Status.InsufficientStorage)
      checkError(builder.NotExtended("hello"), Status.NotExtended)
      checkError(builder.NetworkAuthenticationRequired("hello"), Status.NetworkAuthenticationRequired)
    }

    it("ok with message - Buf") {
      statusAndContentFrom(builder.Ok(Bufs.utf8Buf(message))) shouldBe(Status.Ok, message)
    }

    //TODO UNCOMMENT
//    it("ok with message - Reader") {
//      val reader = Reader.writable()
//      val okBuilder = builder.Ok(reader)
//      reader.write(Bufs.utf8Buf(message)).ensure(reader.close())
//      Await.result(okBuilder.reader.read(message.length)).map(Bufs.asUtf8String).get shouldBe message
//    }

    it("ok with message - ChannelBuffer") {
      statusAndContentFrom(builder.Ok(copiedBuffer(message, defaultCharset()))) shouldBe(Status.Ok, message)
    }

    it("builds Ok with custom type") {
      statusAndContentFrom(builder.Ok(10)) shouldBe(Status.Ok, "10")
    }

    it("content - OutputStream") {
      statusAndContentFrom(builder.HttpResponse().withContent((out: OutputStream) => out.write(message.getBytes()))) shouldBe(Status.Ok, message)
      val streamFn = (out: OutputStream) => out.write(message.getBytes())
      statusAndContentFrom(builder.Ok(streamFn)) shouldBe(Status.Ok, message)
    }

    it("content - String") {
      statusAndContentFrom(builder.HttpResponse().withContent(message)) shouldBe(Status.Ok, message)
      statusAndContentFrom(builder.Ok(message)) shouldBe(Status.Ok, message)
    }

    it("content - Buf") {
      statusAndContentFrom(builder.HttpResponse().withContent(Bufs.utf8Buf(message))) shouldBe(Status.Ok, message)
      statusAndContentFrom(builder.Ok(Bufs.utf8Buf(message))) shouldBe(Status.Ok, message)
    }

    it("content - ChannelBuffer") {
      statusAndContentFrom(builder.HttpResponse().withContent(copiedBuffer(message, defaultCharset()))) shouldBe(Status.Ok, message)
      statusAndContentFrom(builder.Ok(copiedBuffer(message, defaultCharset()))) shouldBe(Status.Ok, message)
    }

    it("error with message - String") {
      statusAndContentFrom(builder.HttpResponse(Status.NotFound).withContent(message)) shouldBe(Status.NotFound, message)
      statusAndContentFrom(builder.NotFound(message))._1 shouldBe Status.NotFound
    }

    it("error with custom type") {
      statusAndContentFrom(builder.HttpResponse(Status.NotFound).withContent(5)) shouldBe(Status.NotFound, "5")
      statusAndContentFrom(builder.NotFound(5)) shouldBe(Status.NotFound, 5.toString)
    }

    it("error with message - Buf") {
      statusAndContentFrom(builder.HttpResponse(Status.NotFound).withContent(Buf.Utf8(5.toString))) shouldBe(Status.NotFound, "5")
      statusAndContentFrom(builder.NotFound(Buf.Utf8(5.toString))) shouldBe(Status.NotFound, 5.toString)
    }

    //    it("error with message - Reader") {
    //      statusAndContentFrom(bldr.NotFound(Reader.fromBuf(Buf.Utf8(expectedErrorContent)))) shouldBe(Status.NotFound, expectedErrorContent)
    //    }

    it("error with message - ChannelBuffer") {
      statusAndContentFrom(builder.HttpResponse(Status.NotFound).withContent(copiedBuffer(message, defaultCharset()))) shouldBe(Status.NotFound, message)
      statusAndContentFrom(builder.NotFound(copiedBuffer(message, defaultCharset()))) shouldBe(Status.NotFound, message)
    }

    it("errors - message") {
      statusAndContentFrom(builder.HttpResponse(Status.BadGateway).withErrorMessage(message).build()) shouldBe(Status.BadGateway, message.length.toString)
      statusAndContentFrom(builder.NotFound(message)) shouldBe(Status.NotFound, message.length.toString)
    }

    it("errors - exception") {
      statusAndContentFrom(builder.HttpResponse(Status.BadGateway).withError(new RuntimeException(message))) shouldBe(Status.BadGateway, message.length.toString)
      statusAndContentFrom(builder.NotFound(new RuntimeException(message))) shouldBe(Status.NotFound, message.length.toString)
    }
  }

}
