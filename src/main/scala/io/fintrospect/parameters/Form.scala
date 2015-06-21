package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

/**
 * Simple wrapper for retrieving the form fields from a request. Alternatively, just get them directly!
 */
class Form(fields: FormField[_] with Retrieval[_, HttpRequest]*) {
}
