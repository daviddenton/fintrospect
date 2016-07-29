package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.Module.ServiceBinding
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}

object ProxyModule {
  def apply(name: String, service: Service[Request, Response], description: String = null): ProxyModule = {
    val descriptionOption = if(description == null) Option(s"Proxy services for $name API") else Option(description)
    ProxyModule(name, service, ModuleSpec(Root, Swagger2dot0Json(ApiInfo(name, name, descriptionOption))))
  }
}

case class ProxyModule private(name: String, service: Service[Request, Response], spec: ModuleSpec[Request, Response]) extends Module {

  def withRoute(i: IncompletePath): ProxyModule = copy(spec = spec.withRoute(i bindToProxy service))

  override def toService = spec.toService

  override protected[fintrospect] def serviceBinding: ServiceBinding = spec.serviceBinding
}