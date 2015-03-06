package io.github.daviddenton.fintrospect.swagger

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpMethod

case class SwPathMethod(method: HttpMethod, summary: String, params: Seq[SwParameter], responses: Seq[SwResponse], securities: Seq[SwSecurity])