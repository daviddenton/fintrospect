package io.fintrospect.util.json

import argo.jdom.{JsonField, JsonNode, JsonRootNode}

object ArgoJsonResponseBuilder extends JsonResponseBuilder[JsonRootNode, JsonNode, JsonField](ArgoJsonFormat)

