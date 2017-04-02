+++
title = "http clients"
tags = ["client", "ssl", "secure", "finagle api", "getting started"]
categories = ["recipe"]
+++

Unlike traditional HTTP client libraries, **Finagle HTTP clients are configured to only talk to a single service** using one or more hosts that are known at creation time of that client. 

### Simple HTTP
```scala
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

val client = Http.newService("pokeapi.co:80")
val futureOk = client(Request("/")) // eventually yields some JSON from the pokemon api
```

### HTTP 1.1 compliance
Currently, Finagle does not add the HTTP-required Host header by default - this can cause problems talking to some HTTP services. To work around this, we can use a Fintrospect provided `Filter`:
```scala
import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.util.Await.result
import io.fintrospect.configuration.{Host, Port}
import io.fintrospect.filters.RequestFilters.AddHost

val authority = Host("pokeapi.co").toAuthority(Port._80)
val client = AddHost(authority).andThen(Http.newService(authority.toString))
val futureOk = client(Request("/")) // eventually yields some JSON from the pokemon api
```

### Secure communication using TLS
TLS support needs to be explicitly enabled for the HTTP codec. Below we are also using a Fintrospect `Filter` to add the GitHub required `User-Agent` header to all requests:
```scala
import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.util.Await.result
import io.fintrospect.filters.RequestFilters.AddUserAgent

val client = Http.client.withTls("api.github.com").newService("api.github.com:443")

val request = Request("/users/daviddenton/repos")

AddUserAgent("Fintrospect client").andThen(client)(request) // eventually yields some json
```

## Further reading
<a name="reading"></a>

- Finagle Clients [guide](https://twitter.github.io/finagle/guide/Clients.html)
