package io.fintrospect.formats

import java.io.OutputStream

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.{Response, Status}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffer

import scala.language.implicitConversions

/**
  * Convenience methods for building Http Responses
  */
trait AbstractResponseBuilder[T] {

  object implicits {
    /**
      * Implicitly convert a ResponseBuilder object to a Future[Response]
      */
    implicit def responseBuilderToFuture(builder: ResponseBuilder[T]): Future[Response] = builder.toFuture

    /**
      * Implicitly convert a Response object to a Future[Response]
      */
    implicit def responseToFuture(response: Response): Future[Response] = Future.value(response)

    /**
      * Implicitly convert a Status object to a correctly generified ResponseBuilderConfig
      */
    implicit def statusToResponseBuilderConfig(status: Status): ResponseBuilderConfig = new ResponseBuilderConfig(status)

    /**
      * Implicitly convert a ResponseBuilder object to a Response
      */
    implicit def responseBuilderToResponse(builder: ResponseBuilder[T]): Response = builder.build()
  }

  /**
    * Intermediate object used when constructing a ResponseBuilder from a Status object (via implicit conversion)
    */
  class ResponseBuilderConfig(status: Status) {
    def apply(): ResponseBuilder[T] = HttpResponse(status)

    def apply(f: OutputStream => Unit): ResponseBuilder[T] = HttpResponse(status).withContent(f)

    def apply(channelBuffer: ChannelBuffer): ResponseBuilder[T] = HttpResponse(status).withContent(channelBuffer)

    def apply(content: String): ResponseBuilder[T] = if (status.code < 400) HttpResponse(status).withContent(content) else HttpResponse(status).withErrorMessage(content)

    def apply(buf: Buf): ResponseBuilder[T] = HttpResponse(status).withContent(buf)

    def apply(stream: AsyncStream[T]): ResponseBuilder[T] = HttpResponse(status).withContent(stream)

    def apply(reader: Reader): ResponseBuilder[T] = HttpResponse(status).withContent(reader)

    def apply(t: T): ResponseBuilder[T] = HttpResponse(status).withContent(t)

    def apply(error: Throwable): ResponseBuilder[T] = HttpResponse(status).withError(error)
  }

  def HttpResponse(): ResponseBuilder[T]

  def HttpResponse(status: Status): ResponseBuilder[T] = HttpResponse().withCode(status)

  def Continue(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Continue))

  def SwitchingProtocols(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.SwitchingProtocols))

  def Processing(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Processing))

  def Ok(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Ok))

  def Created(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Created))

  def Accepted(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Accepted))

  def NonAuthoritativeInformation(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NonAuthoritativeInformation))

  def NoContent(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NoContent))

  def ResetContent(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ResetContent))

  def PartialContent(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PartialContent))

  def MultiStatus(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MultiStatus))

  def MultipleChoices(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MultipleChoices))

  def MovedPermanently(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MovedPermanently))

  def Found(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Found))

  def SeeOther(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.SeeOther))

  def NotModified(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotModified))

  def UseProxy(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UseProxy))

  def TemporaryRedirect(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.TemporaryRedirect))

  def BadRequest(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.BadRequest))

  def Unauthorized(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Unauthorized))

  def PaymentRequired(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PaymentRequired))

  def Forbidden(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Forbidden))

  def NotFound(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotFound))

  def MethodNotAllowed(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MethodNotAllowed))

  def NotAcceptable(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotAcceptable))

  def ProxyAuthenticationRequired(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ProxyAuthenticationRequired))

  def RequestTimeout(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestTimeout))

  def Conflict(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Conflict))

  def Gone(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Gone))

  def LengthRequired(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.LengthRequired))

  def PreconditionFailed(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PreconditionFailed))

  def RequestEntityTooLarge(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestEntityTooLarge))

  def RequestURITooLong(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestURITooLong))

  def UnsupportedMediaType(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnsupportedMediaType))

  def RequestedRangeNotSatisfiable(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestedRangeNotSatisfiable))

  def ExpectationFailed(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ExpectationFailed))

  def EnhanceYourCalm(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.EnhanceYourCalm))

  def UnprocessableEntity(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnprocessableEntity))

  def Locked(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Locked))

  def FailedDependency(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.FailedDependency))

  def UnorderedCollection(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnorderedCollection))

  def UpgradeRequired(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UpgradeRequired))

  def PreconditionRequired(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PreconditionRequired))

  def TooManyRequests(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.TooManyRequests))

  def RequestHeaderFieldsTooLarge(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestHeaderFieldsTooLarge))

  def UnavailableForLegalReasons(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnavailableForLegalReasons))

  def ClientClosedRequest(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ClientClosedRequest))

  def InternalServerError(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.InternalServerError))

  def NotImplemented(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotImplemented))

  def BadGateway(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.BadGateway))

  def ServiceUnavailable(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ServiceUnavailable))

  def GatewayTimeout(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.GatewayTimeout))

  def HttpVersionNotSupported(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.HttpVersionNotSupported))

  def VariantAlsoNegotiates(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.VariantAlsoNegotiates))

  def InsufficientStorage(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.InsufficientStorage))

  def NotExtended(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotExtended))

  def NetworkAuthenticationRequired(responseMagnet: ResponseMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NetworkAuthenticationRequired))
}
