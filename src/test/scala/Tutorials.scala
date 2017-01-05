object Tutorials {
  //  About finagle
  //    what is it?
  //    futures
  //    services and filters
  //    request and response
  //    path
  //    CODE example of starting a server
  //
  //  What is fintrospect
  //    feature list
  //
  //  Modules
  //    what it is
  // a module is simply a collection of endpoints which are grouped together under a common context (or Path).
  //
  //    CODE Your first module
  import com.twitter.finagle.Http
  import com.twitter.finagle.http.path.Root
  import com.twitter.util.Await.ready
  import io.fintrospect.ResourceLoader.Directory
  import io.fintrospect.StaticModule

  val javascript = StaticModule(Root / "javascript", Directory("/myproject/js"))
  ready(Http.serve("localhost:9000", javascript.toService))

  //    combining modules
  import com.twitter.finagle.Http
  import com.twitter.finagle.http.path.Root
  import com.twitter.util.Await.ready
  import io.fintrospect.ResourceLoader.Directory
  import io.fintrospect.StaticModule

  val css = StaticModule(Root / "styles", Directory("/myproject/css"))
  css.combine(javascript)
  ready(Http.serve("localhost:9000", StaticModule(Root / "javascript", Directory("/myproject/js")).toService))



  /*  Architectural cookbook

    finagle
      concepts
        Future
        Service
        Filter

      simple proxy server

    clients

    handling JSON
    XML
    HTML
    automarshalling

    security
    streaming
    validation

    web
      templating
      static content
      forms
        standard
        multipart

   */


}
