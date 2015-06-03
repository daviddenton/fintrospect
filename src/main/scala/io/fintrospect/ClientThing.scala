package io.fintrospect

import com.twitter.finagle.Service
import io.fintrospect.parameters.{Path, Query}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

class ClientThing {

  val route: (Int) => Service[Int, HttpResponse] = ??? //ClientRoute("what is this about").at(GET) / "bob" / Path.int("someInt")

  //http://www.bob.com/{age}/{name}?gender={gender}

  val underlyingService: Service[HttpRequest, HttpResponse] = ???

  private val lookupByAge: ClientPath1[ClientRoute1[String], Int] = ClientRoute("entitlements")
    .taking(Query.required.string("name"))
    .at(HttpMethod.GET) / "bob" / Path.int("age")



  lookupByAge(123)(underlyingService)
}
