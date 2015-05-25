Release Notes/Migration Guide
-----------------------------

#####v2.X -> v3.X

Migrated away from the in-built Twitter HTTP Request package (com.twitter.finagle.http) and onto the Netty HttpRequest
(org.jboss.netty.handler.codec.http). This is to provide compatibility with the changes to the Finagle APIs in regards
to creating both servers and clients. It also has the advantage of unifying the client/server interface (previously it
was different between the 2). The only things that should have to change in your code are:

  - How servers are created - the new method is simpler (see the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).
  - The signature of routes is now ```Service[HttpRequest,HttpResponse]```. Since the Twitter Request/Response classes
   extends these interfaces, usages of the ResponseBuilder remain the same.
  - Form-based parameters are now defined with the ```Form``` object, and not the ```Query``` object (which now just retrieves Query String parameters from the URL).
