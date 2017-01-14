package io.fintrospect.formats

import com.twitter.finagle.http.Status

import scala.language.implicitConversions

/**
  * Convenience methods for building Http Responses
  */
trait AbstractResponseBuilder[T] {

  def HttpResponse(): ResponseBuilder[T]

  def HttpResponse(status: Status): ResponseBuilder[T] = HttpResponse().withCode(status)

  def Continue(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Continue))

  def SwitchingProtocols(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.SwitchingProtocols))

  def Processing(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Processing))

  def Ok(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Ok))

  def Created(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Created))

  def Accepted(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Accepted))

  def NonAuthoritativeInformation(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NonAuthoritativeInformation))

  def NoContent(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NoContent))

  def ResetContent(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ResetContent))

  def PartialContent(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PartialContent))

  def MultiStatus(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MultiStatus))

  def MultipleChoices(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MultipleChoices))

  def MovedPermanently(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MovedPermanently))

  def Found(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Found))

  def SeeOther(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.SeeOther))

  def NotModified(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotModified))

  def UseProxy(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UseProxy))

  def TemporaryRedirect(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.TemporaryRedirect))

  def BadRequest(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.BadRequest))

  def Unauthorized(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Unauthorized))

  def PaymentRequired(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PaymentRequired))

  def Forbidden(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Forbidden))

  def NotFound(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotFound))

  def MethodNotAllowed(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MethodNotAllowed))

  def NotAcceptable(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotAcceptable))

  def ProxyAuthenticationRequired(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ProxyAuthenticationRequired))

  def RequestTimeout(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestTimeout))

  def Conflict(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Conflict))

  def Gone(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Gone))

  def LengthRequired(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.LengthRequired))

  def PreconditionFailed(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PreconditionFailed))

  def RequestEntityTooLarge(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestEntityTooLarge))

  def RequestURITooLong(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestURITooLong))

  def UnsupportedMediaType(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnsupportedMediaType))

  def RequestedRangeNotSatisfiable(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestedRangeNotSatisfiable))

  def ExpectationFailed(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ExpectationFailed))

  def EnhanceYourCalm(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.EnhanceYourCalm))

  def UnprocessableEntity(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnprocessableEntity))

  def Locked(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Locked))

  def FailedDependency(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.FailedDependency))

  def UnorderedCollection(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnorderedCollection))

  def UpgradeRequired(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UpgradeRequired))

  def PreconditionRequired(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PreconditionRequired))

  def TooManyRequests(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.TooManyRequests))

  def RequestHeaderFieldsTooLarge(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestHeaderFieldsTooLarge))

  def UnavailableForLegalReasons(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnavailableForLegalReasons))

  def ClientClosedRequest(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ClientClosedRequest))

  def InternalServerError(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.InternalServerError))

  def NotImplemented(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotImplemented))

  def BadGateway(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.BadGateway))

  def ServiceUnavailable(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ServiceUnavailable))

  def GatewayTimeout(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.GatewayTimeout))

  def HttpVersionNotSupported(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.HttpVersionNotSupported))

  def VariantAlsoNegotiates(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.VariantAlsoNegotiates))

  def InsufficientStorage(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.InsufficientStorage))

  def NotExtended(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotExtended))

  def NetworkAuthenticationRequired(responseMagnet: ResponseContentMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NetworkAuthenticationRequired))
}
