Release Notes/Migration Guide
-----------------------------

#####v3.X -> v4.X

- Addition of custom response rendering formats. See the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).
  Modules now require a fully configured ```ModuleRenderer``` upon instantiation, which provides not just the rendering of the Documentation (ie. the old ```Renderer``` which is now ```DescriptionRenderer```), but also the format of error messages.

#####v2.X -> v3.X

Migrated away from the in-built Twitter HTTP Request package (```com.twitter.finagle.http```) and onto the Netty ```
org.jboss.netty.handler.codec.http.HttpRequest```. This is to provide compatibility with the changes to the Finagle APIs in regards
to creating both servers and clients. It also has the advantage of unifying the client/server interface (previously it
was different between the two). The only things that should have to change in your code are:

  - How servers are created - the new method is simpler (see the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).
  - The signature of routes is now ```Service[HttpRequest,HttpResponse]```. Since the Twitter Request/Response classes
   extends these interfaces, usages of the ResponseBuilder remain the same.
  - Form-based parameters are now defined with the ```Form``` object, and not the ```Query``` object (which now just retrieves Query String parameters from the URL).
