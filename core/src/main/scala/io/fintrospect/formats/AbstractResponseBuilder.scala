package io.fintrospect.formats

import com.twitter.finagle.http.Status

import scala.language.implicitConversions

/**
  * Convenience methods for building Http Responses
  */
trait AbstractResponseBuilder[T] {

  def HttpResponse(): ResponseBuilder[T]

  def HttpResponse(status: Status): ResponseBuilder[T] = HttpResponse().withCode(status)

  def Continue(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Continue))

  def SwitchingProtocols(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.SwitchingProtocols))

  def Processing(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Processing))

  def Ok(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Ok))

  def Created(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Created))

  def Accepted(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Accepted))

  def NonAuthoritativeInformation(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NonAuthoritativeInformation))

  def NoContent(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NoContent))

  def ResetContent(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ResetContent))

  def PartialContent(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PartialContent))

  def MultiStatus(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MultiStatus))

  def MultipleChoices(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MultipleChoices))

  def MovedPermanently(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MovedPermanently))

  def Found(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Found))

  def SeeOther(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.SeeOther))

  def NotModified(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotModified))

  def UseProxy(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UseProxy))

  def TemporaryRedirect(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.TemporaryRedirect))

  def BadRequest(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.BadRequest))

  def Unauthorized(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Unauthorized))

  def PaymentRequired(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PaymentRequired))

  def Forbidden(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Forbidden))

  def NotFound(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotFound))

  def MethodNotAllowed(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.MethodNotAllowed))

  def NotAcceptable(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotAcceptable))

  def ProxyAuthenticationRequired(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ProxyAuthenticationRequired))

  def RequestTimeout(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestTimeout))

  def Conflict(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Conflict))

  def Gone(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Gone))

  def LengthRequired(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.LengthRequired))

  def PreconditionFailed(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PreconditionFailed))

  def RequestEntityTooLarge(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestEntityTooLarge))

  def RequestURITooLong(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestURITooLong))

  def UnsupportedMediaType(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnsupportedMediaType))

  def RequestedRangeNotSatisfiable(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestedRangeNotSatisfiable))

  def ExpectationFailed(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ExpectationFailed))

  def EnhanceYourCalm(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.EnhanceYourCalm))

  def UnprocessableEntity(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnprocessableEntity))

  def Locked(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.Locked))

  def FailedDependency(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.FailedDependency))

  def UnorderedCollection(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnorderedCollection))

  def UpgradeRequired(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UpgradeRequired))

  def PreconditionRequired(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.PreconditionRequired))

  def TooManyRequests(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.TooManyRequests))

  def RequestHeaderFieldsTooLarge(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.RequestHeaderFieldsTooLarge))

  def UnavailableForLegalReasons(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.UnavailableForLegalReasons))

  def ClientClosedRequest(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ClientClosedRequest))

  def InternalServerError(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.InternalServerError))

  def NotImplemented(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotImplemented))

  def BadGateway(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.BadGateway))

  def ServiceUnavailable(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.ServiceUnavailable))

  def GatewayTimeout(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.GatewayTimeout))

  def HttpVersionNotSupported(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.HttpVersionNotSupported))

  def VariantAlsoNegotiates(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.VariantAlsoNegotiates))

  def InsufficientStorage(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.InsufficientStorage))

  def NotExtended(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NotExtended))

  def NetworkAuthenticationRequired(responseMagnet: ResponseBuilderMagnet[T]): ResponseBuilder[T] = responseMagnet(HttpResponse(Status.NetworkAuthenticationRequired))
}
